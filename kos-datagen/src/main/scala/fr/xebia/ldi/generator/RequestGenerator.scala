package fr.xebia.ldi.generator

import java.time.Instant

import akka.actor.{Actor, ActorRef}
import akka.kafka.ProducerMessage
import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader}
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalacheck.Gen
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
  * Created by loicmdivad.
  */
class RequestGenerator(responseGen: ActorRef, publisher: ActorRef)(implicit dispatcher: ExecutionContextExecutor) extends Actor {

  private val logger = LoggerFactory.getLogger(getClass)

  val generator: Gen[(FrameHeader, FrameBody)] = for {
    t <- Gen.choose(1, 3)
    num <- Gen.chooseNum(12, 97)
    label <- Gen.frequency((3, "EU"), (6, "US"), (2, "ZG"))

  } yield (
    new FrameHeader(s"T0$t", Instant.now().getEpochSecond, s"0$num:$label".mkString),
    new FrameBody(false, Vector.empty[String].asJava, s"0$num:$label".mkString)
  )

  override def receive: Receive = {
    case _ =>
      val dt = Gen.choose(0.1, 0.3).sample.get
      context.system.scheduler.scheduleOnce(dt nanoseconds, self, generate())
  }

  def generate(): Unit = {

    generator.sample.foreach {
      case (header: FrameHeader, body: FrameBody) =>
        val record = new ProducerRecord[FrameHeader, FrameBody]("GAME-FRAME-RQ", header, body)
        publisher ! ProducerMessage.Message[FrameHeader, FrameBody, Long](record, header.getTs)
        responseGen ! (header, body)

      case message =>
        logger warn "Unknown tuple (header, body) send by the generator:  " + message
    }
  }

}
