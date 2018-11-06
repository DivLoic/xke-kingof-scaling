package fr.xebia.ldi.common

import fr.xebia.ldi.common.frame.KoSByteModel
import scodec.codecs.{Discriminated, Discriminator}

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
}