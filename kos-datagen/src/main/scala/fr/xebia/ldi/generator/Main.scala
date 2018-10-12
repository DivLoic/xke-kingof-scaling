package fr.xebia.ldi.generator

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory
import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader}
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde

import scala.collection.JavaConverters._

/**
  * Created by loicmdivad.
  */
object Main extends App {

  val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val headerSerde: SpecificAvroSerde[FrameHeader] = new SpecificAvroSerde()

  val bodySerde: SpecificAvroSerde[FrameBody] = new SpecificAvroSerde()

  val props = Map("schema.registry.url" -> config.getString("schema.registry.url"))

  headerSerde.configure(props.asJava, true)

  bodySerde.configure(props.asJava, false)

  val setting: ProducerSettings[FrameHeader, FrameBody] = ProducerSettings(
    system, headerSerde.serializer(), bodySerde.serializer()
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
