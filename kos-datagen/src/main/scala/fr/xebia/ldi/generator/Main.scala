package fr.xebia.ldi.generator

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader}
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}

import scala.collection.JavaConverters._
import fr.xebia.ldi.generator.config.StreamingAppConfig.StreamingAppConfig
import org.apache.log4j.Logger

/**
  * Created by loicmdivad.
  */
object Main extends App {

  val logger = Logger.getLogger(getClass)

  import fr.xebia.ldi.generator.config.ConfluentConfig.confluentConfigReader

  pureconfig.loadConfig[StreamingAppConfig]("producer") match {

    case scala.Left(failures) =>
      failures.toList.foreach(failure => println(failure.description))

    case scala.Right(config) =>

      implicit val system: ActorSystem = ActorSystem()

      implicit val materializer: ActorMaterializer = ActorMaterializer()

      val headerSerde: SpecificAvroSerde[FrameHeader] = new SpecificAvroSerde()

      val bodySerde: SpecificAvroSerde[FrameBody] = new SpecificAvroSerde()

      val props = Map("schema.registry.url" -> config.schemaRegistryUrl)

      headerSerde.configure(props.asJava, true)

      bodySerde.configure(props.asJava, false)

      val setting: ProducerSettings[FrameHeader, FrameBody] = kafkaProperties(
        ProducerSettings(system, headerSerde.serializer(), bodySerde.serializer()), config
      )

      val producerRef: ActorRef =
        Source.actorRef[Message[FrameHeader, FrameBody, Long]](10, OverflowStrategy.dropBuffer)
          .via(Producer.flexiFlow(setting))
          .to(Sink.ignore)
          .run()

      val rsGen = system.actorOf(Props.apply(classOf[ResponseGenerator], producerRef, system.dispatcher))
      val rqGen = system.actorOf(Props.apply(classOf[RequestGenerator], rsGen, producerRef, system.dispatcher))

      rqGen ! "Start"

  }

  def kafkaProperties[K, V](setting: ProducerSettings[K, V], config: StreamingAppConfig): ProducerSettings[K, V] = {
    val basicProps = Map(
      "bootstrap.servers" -> config.cloud.map(_.bootstrapServers).getOrElse("localhost:9092"),
      "default.key.serde" -> classOf[GenericAvroSerde].toString,
      "default.value.serde" -> classOf[GenericAvroSerde].toString,
      "schema.registry.url" -> config.schemaRegistryUrl
    )

    val cloudProps = config.cloud.map { cc =>
      Map(
        "replication.factor" -> "3",
        "sasl.mechanism" -> cc.saslMechanism,
        "request.timeout.ms" -> cc.requestTimeoutMs.toString,
        "retry.backoff.ms" -> cc.retryBackoffMs.toString,
        "sasl.jaas.config" -> cc.saslJaasConfig,
        "security.protocol" -> cc.securityProtocol,
        "ssl.endpoint.identification.algorithm" -> cc.sslEndpointIdentificationAlgorithm
      )
    }

    setting
      .withProperties(basicProps)
      .withProperties(cloudProps.getOrElse(Map.empty))
  }
}
