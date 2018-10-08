package fr.xebia.ldi.stream

import java.util.Properties

import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader, GameAction, GameSession}
import fr.xebia.ldi.stream.config.StreamingAppConfig.StreamingAppConfig
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.apache.log4j.Logger

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import scala.concurrent.duration._

import scala.collection.JavaConverters._

/**
  * Created by loicmdivad.
  */
object Main extends App {

  val logger = Logger.getLogger(getClass)

  import fr.xebia.ldi.stream.config.ConfluentConfig._

  pureconfig.loadConfig[StreamingAppConfig]("streams") match {

    case Left(failures) =>
      failures.toList.foreach(failure => logger.error(failure.description))

    case Right(config) => {

      val serdeProp = schemaRegistryProps(config).asJava

      val bodySerde = new SpecificAvroSerde[FrameBody]
      bodySerde.configure(serdeProp, false)

      val headerSerde = new SpecificAvroSerde[FrameHeader]
      headerSerde.configure(serdeProp, false)

      val actionSerde = new SpecificAvroSerde[GameAction]
      actionSerde.configure(serdeProp, false)

      val sessionSerde = new SpecificAvroSerde[GameSession]
      sessionSerde.configure(serdeProp, false)

      val builder = new StreamsBuilder

      val requests: KStream[FrameHeader, FrameBody] =
        builder.stream(config.topics.request, Consumed.`with`(headerSerde, bodySerde))

      val responses: KStream[FrameHeader, FrameBody] =
        builder.stream(config.topics.response, Consumed.`with`(headerSerde, bodySerde))

      val valuejoiner: ValueJoiner[FrameBody, FrameBody, GameAction] =
        (rqBody: FrameBody, rsBody: FrameBody) => new GameAction("Game", rqBody.getMachine)

      val correlated = requests.join[FrameBody, GameAction](
        responses,
        valuejoiner,
        JoinWindows.of(1.second.toMillis),
        Joined.`with`(headerSerde, bodySerde, bodySerde)
      )

      val Array(type1, type2, type3) =  correlated.branch(
        (key: FrameHeader, _: GameAction) => key.getType == "T01",
        (key: FrameHeader, _: GameAction) => key.getType == "T02",
        (key: FrameHeader, _: GameAction) => key.getType == "T03"
      )

      val sessionInit: Initializer[GameSession] = () => new GameSession("")

      val sessionAggregator: Aggregator[FrameHeader, GameAction, GameSession] =
        (key: FrameHeader, value: GameAction, aggregate: GameSession) =>
          new GameSession(s"${aggregate.getFrames}#${value.getMachine}")

      val sessions: KTable[FrameHeader, GameSession] = type1.groupByKey.aggregate(
        sessionInit,
        sessionAggregator,
        Materialized.`with`(headerSerde, sessionSerde)
      )

      sessions.toStream.to("SESSIONS", Produced.`with`(headerSerde, sessionSerde))

      val kafkaStreams = new KafkaStreams(builder.build, kafkaStreamProps(config))

      kafkaStreams.start()
    }
  }

  def schemaRegistryProps(config: StreamingAppConfig) =
    Map("schema.registry.url" -> config.schemaRegistryUrl)

  def kafkaStreamProps(config: StreamingAppConfig): Properties = {
    val properties = new Properties()
    Map(

      StreamsConfig.BOOTSTRAP_SERVERS_CONFIG -> config.cloud.map(_.bootstrapServers).getOrElse("localhost:9092"),
      StreamsConfig.APPLICATION_ID_CONFIG -> config.appId,
      StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG -> classOf[GenericAvroSerde],
      StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG -> classOf[GenericAvroSerde],
      StreamsConfig.COMMIT_INTERVAL_MS_CONFIG -> "100",
      StreamsConfig.REPLICATION_FACTOR_CONFIG -> "1",
      "schema.registry.url" -> config.schemaRegistryUrl

    ).foreach(kv => properties.put(kv._1, kv._2))
    properties
  }


}
