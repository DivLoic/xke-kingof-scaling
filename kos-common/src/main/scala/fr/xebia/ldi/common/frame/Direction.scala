package fr.xebia.ldi.common.frame

import fr.xebia.ldi.common.schema.{Direction => JDirection}
import fr.xebia.ldi.common.schema.Direction.{UP => JUp,  DOWN => JDown,  LEFT => JLeft,  RIGHT => JRight}
import fr.xebia.ldi.common.frame.KoSByteModel.kosByteCodec
import scodec.codecs._
import scodec.codecs.Discriminator


/**
  * Created by loicmdivad.
  */
sealed trait Direction

object Direction {

  case class Up() extends Direction
  case class Down() extends Direction
  case class Left() extends Direction
  case class Right() extends Direction

  implicit val discriminated: Discriminated[Direction, KoSByteModel] = Discriminated(kosByteCodec)

  object Up {
    private val upKey = KoSByteModel(0x8A.toByte)
    implicit val discriminator: Discriminator[Direction, Up, KoSByteModel] = Discriminator(upKey)
  }

  object Down {
    private val downKey = KoSByteModel(0x8C.toByte)
    implicit val discriminator: Discriminator[Direction, Down, KoSByteModel] = Discriminator(downKey)
  }

  object Left {
    private val leftKey = KoSByteModel(0x8D.toByte)
    implicit val discriminator: Discriminator[Direction, Left, KoSByteModel] = Discriminator(leftKey)
  }

  object Right {
    private val rightKey = KoSByteModel(0x8E.toByte)
    implicit val discriminator: Discriminator[Direction, Right, KoSByteModel] = Discriminator(rightKey)
  }


  implicit class DirectionJavaConverter(value: Direction) {

    def asJava: JDirection = value match {
      case Up() => JUp
      case Down() => JDown
      case Left() => JLeft
      case Right() => JRight
    }
  }
}