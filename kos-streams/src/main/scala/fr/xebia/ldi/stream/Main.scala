package fr.xebia.ldi.stream

import java.util.Properties

import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader, GameAction, GameSession, Hit => JHit}
import fr.xebia.ldi.stream.config.StreamingAppConfig.StreamingAppConfig
import fr.xebia.ldi.stream.operator.{StateFull, StateLess}
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.apache.log4j.Logger

import scala.collection.JavaConverters._


/**
  * Created by loicmdivad.
  */
object Main extends App {

  val logger = Logger.getLogger(getClass)

  import fr.xebia.ldi.stream.config.ConfluentConfig.confluentConfigReader

  pureconfig.loadConfig[StreamingAppConfig]("streams") match {

    case scala.Left(failures) =>
      failures.toList.foreach(failure => logger.error(failure.description))

    case scala.Right(config) =>

      val serdeProp = schemaRegistryProps(config).asJava

      val bodySerde = new SpecificAvroSerde[FrameBody]
      bodySerde.configure(serdeProp, false)

      val headerSerde = new SpecificAvroSerde[FrameHeader]
      headerSerde.configure(serdeProp, false)

      val actionSerde = new SpecificAvroSerde[GameAction]
      actionSerde.configure(serdeProp, false)

      val sessionSerde = new SpecificAvroSerde[GameSession]
      sessionSerde.configure(serdeProp, false)

      val hitSerde = new SpecificAvroSerde[JHit]
      hitSerde.configure(serdeProp, false)

      val builder = new StreamsBuilder

      val requests: KStream[FrameHeader, FrameBody] =
        builder.stream(config.topics.request, Consumed.`with`(headerSerde, bodySerde))

      val responses: KStream[FrameHeader, FrameBody] =
        builder.stream(config.topics.response, Consumed.`with`(headerSerde, bodySerde))

      val correlated: KStream[FrameHeader, FrameBody] = requests.join[FrameBody, FrameBody](
        responses,
        StateFull.reqResponseJoiner,
        JoinWindows.of(StateFull.joinWindowSize toMillis),
        Joined.`with`(headerSerde, bodySerde, bodySerde)
      )

      val flattenCorrelated: KStream[FrameHeader, JHit] = correlated.flatMapValues(StateLess.flatMapDecoder)

      val sessions: KTable[FrameHeader, GameSession] = flattenCorrelated.groupByKey.aggregate(
        StateFull.sessionInitializer,
        StateFull.sessionAggregator,
        Materialized.`with`(headerSerde, sessionSerde)
      )

      sessions.toStream.to(config.topics.output, Produced.`with`(headerSerde, sessionSerde))

      val kafkaStreams = new KafkaStreams(builder.build, kafkaStreamProps(config))

      kafkaStreams.start()

  }

  def schemaRegistryProps(config: StreamingAppConfig) =
    Map("schema.registry.url" -> config.schemaRegistryUrl)

  def kafkaStreamProps(config: StreamingAppConfig): Properties = {
    val properties = new Properties()

    Map(
      StreamsConfig.BOOTSTRAP_SERVERS_CONFIG -> config.cloud.map(_.bootstrapServers).getOrElse("localhost:9092"),
      StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG -> classOf[GenericAvroSerde],
      StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG -> classOf[GenericAvroSerde],
      StreamsConfig.APPLICATION_ID_CONFIG -> config.appId,
      StreamsConfig.COMMIT_INTERVAL_MS_CONFIG -> "100",
      StreamsConfig.REPLICATION_FACTOR_CONFIG -> "1",
      StreamsConfig.NUM_STREAM_THREADS_CONFIG -> "1",
      "schema.registry.url" -> config.schemaRegistryUrl,

    ).foreach(kv => properties.put(kv._1, kv._2))

    config.cloud.foreach { cc =>
      Map(
        StreamsConfig.REPLICATION_FACTOR_CONFIG -> "3",
        "sasl.mechanism" -> cc.saslMechanism,
        "request.timeout.ms" -> cc.requestTimeoutMs.toString,
        "retry.backoff.ms" -> cc.retryBackoffMs.toString,
        "sasl.jaas.config" -> cc.saslJaasConfig,
        "security.protocol" -> cc.securityProtocol,
        "ssl.endpoint.identification.algorithm" -> cc.sslEndpointIdentificationAlgorithm
      ).foreach(kv => properties.put(kv._1, kv._2))
    }
    properties
  }
}
