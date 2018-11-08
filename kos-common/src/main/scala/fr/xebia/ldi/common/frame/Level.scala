package fr.xebia.ldi.common.frame

import scodec.codecs.{Discriminated, Discriminator}
import fr.xebia.ldi.common.schema.{Level => JLevel}
import fr.xebia.ldi.common.schema.Level.{Inter => JInter, NewBie => JNewBie, Pro => JPro}

/**
  * Created by loicmdivad.
  */
sealed trait Level

object Level {

  case class Pro() extends Level
  case class Inter() extends Level
  case class NewBie() extends Level

  implicit val discriminated: Discriminated[Level, KoSByteModel] = Discriminated(KoSByteModel.kosByteCodec)

  object Pro {
    val proByte = KoSByteModel(0xF9.toByte)
    implicit val discriminator: Discriminator[Level, Pro, KoSByteModel] = Discriminator(proByte)
  }
  object Inter {
    val interByte = KoSByteModel(0xE9.toByte)
    implicit val discriminator: Discriminator[Level, Inter, KoSByteModel] = Discriminator(interByte)
  }
  object NewBie {
    val newBieByte = KoSByteModel(0xD9.toByte)
    implicit val discriminator: Discriminator[Level, NewBie, KoSByteModel] = Discriminator(newBieByte)
  }

  implicit class LevelScalaConverter(value: JLevel) {
    def asScala: Level = value match {
      case JPro => Pro()
      case JInter => Inter()
      case JNewBie => NewBie()
    }
  }

  implicit class LevelJavaConverter(value: Level) {
    def asJava: JLevel = value match {
      case Pro() => JPro
      case Inter() => JInter
      case NewBie() => JNewBie
    }
  }
}