package fr.xebia.ldi.generator

import java.time.Instant

import akka.actor.{Actor, ActorRef}
import akka.kafka.ProducerMessage
import fr.xebia.ldi.common.frame.Direction.{Down, Left, Right, Up}
import fr.xebia.ldi.common.frame.Game.GameScalaConverter
import fr.xebia.ldi.common.frame.Impact.{Critical, Direct, Fatal, Missed, Special}
import fr.xebia.ldi.common.frame.Key.{O, X}
import fr.xebia.ldi.common.frame.Level.LevelScalaConverter
import fr.xebia.ldi.common.frame._
import fr.xebia.ldi.common.schema.Game.Neowave
import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader, Game => JGame, Level => JLevel, Zone => JZone}
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalacheck.Gen
import org.slf4j.LoggerFactory
import scodec.Codec

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scodec.codecs.implicits._


/**
  * Created by loicmdivad.
  */
class RequestGenerator(responseGen: ActorRef, publisher: ActorRef)(implicit dispatcher: ExecutionContextExecutor) extends Actor {

  import RequestGenerator._

  type Frame = (FrameHeader, FrameBody)

  private val logger = LoggerFactory.getLogger(getClass)

  val machines: Vector[Machine] = {
    val genMachines = for {
      max <- Gen.choose(MinMachineNum, MinMaxMachineNum)
      games <- Gen.listOfN(max, Gen.oneOf(JGame.values()))
      zones <- Gen.listOfN(max, Gen.oneOf(JZone.values()))
      ids <- Gen.listOfN(max, Gen.choose(MinMachineId, MaxMachineId))

    } yield ids.zip(zones).zip(games).map {
      case ((id, zone), game) => Machine(id, zone, game)
    }

    genMachines.sample match {
      case Some(generated) => generated.toVector
      case None => logger warn "Fail to generate a new group. Use the default machine group."
        DefaultMachineGroup
    }
  }

  val genFrame: Gen[Frame] = for {
    machine <- Gen.oneOf(machines)
    level <- Gen.oneOf(JLevel.values())
    hitNum <- Gen.choose(1, MaxHitNumber)

    hits <- Gen.listOfN(hitNum, for {
      key <- Gen.frequency(keyFrequency: _*)
      violence <- Gen.choose[Short](MinViolence, MaxViolence)
      direction <- Gen.frequency[Direction](directionFrequency: _*)
      impact <- Gen.frequency[Impact](impactFrequency(violence): _*)
    } yield new Hit(key, Some(direction), impact, None, level.asScala, machine.game.asScala))

  } yield (
    new FrameHeader(Instant.now().toEpochMilli(), padLeft(machine.id), machine.zone),
    new FrameBody(false, hits.map(Codec.encode(_).require.toHex).asJava)
  )

  override def receive: Receive = {
    case _ =>

      val dt = Gen.choose(0.1, 0.3).sample.get

      context.system.scheduler.scheduleOnce(dt nanoseconds, self, generate())
  }

  def generate(): Unit = {

    genFrame.sample.foreach {
      case (header: FrameHeader, body: FrameBody) =>
        val record = new ProducerRecord[FrameHeader, FrameBody]("GAME-FRAME-RQ", header, body)
        publisher ! ProducerMessage.Message[FrameHeader, FrameBody, Long](record, header.getTs)
        responseGen ! (header, body)

      case message =>
        logger warn "Unknown tuple (header, body) send by the generator:  " + message
    }
  }

}

object RequestGenerator {

  val MinMachineNum = 10
  val MinMaxMachineNum = 35

  val MinMachineId = 4
  val MaxMachineId = 999999

  val MinViolence = 42 toShort
  val MaxViolence = 209 toShort

  val MaxHitNumber = 5

  case class Machine(id: Int = 0, zone: JZone, game: JGame = Neowave)

  val DefaultMachineGroup = Vector(
    Machine(zone = JZone.EU),
    Machine(id = 1, zone = JZone.AU),
    Machine(id = 2, zone = JZone.US),
    Machine(id = 3, zone = JZone.ZG)
  )

  def padLeft(value: Int) = f"$value%06d"

  def keyFrequency: Vector[(Int, Gen[Key])] = Vector(
    (1, X()),
    (1, O())
  )

  def directionFrequency: Vector[(Int, Gen[Direction])] = Vector(
    (4, Up()),
    (3, Right()),
    (2, Down()),
    (1, Left())
  )

  def impactFrequency(violence: Short): Vector[(Int, Gen[Impact])] = Vector(
    (1, Fatal(violence)),
    (2, Critical(violence)),
    (2, Special(violence)),
    (10, Direct(violence)),
    (5, Missed())
  )
}