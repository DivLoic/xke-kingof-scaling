package fr.xebia.ldi.generator

import java.time.Instant

import akka.actor.{Actor, ActorRef}
import akka.kafka.ProducerMessage
import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader}
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalacheck.Gen

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by loicmdivad.
  */
class ResponseGenerator(publisher: ActorRef)(implicit dispatcher: ExecutionContextExecutor) extends Actor {

  override def receive: Receive = {
    case (header: FrameHeader, body: FrameBody) =>
      Gen.frequency((100, true), (1, false)).sample match {
        case Some(true) =>
          body.setApproval(true)
          val record = new ProducerRecord[FrameHeader, FrameBody](
            "GAME-FRAME-RS", header, body
          )
          publisher ! ProducerMessage.Message[FrameHeader, FrameBody, Long](record, Instant.now().getEpochSecond)
        case _ =>
      }
    case _ =>
  }

}
