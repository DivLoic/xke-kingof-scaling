package fr.xebia.ldi.common.frame

import fr.xebia.ldi.common.schema.{Key => JKey}
import fr.xebia.ldi.common.schema.Key.{X => JX, O => JO}
import scodec.codecs._

/**
  * Created by loicmdivad.
  */
sealed trait Key

object Key {

  case class X() extends Key

  case class O() extends Key

  implicit val discriminated: Discriminated[Key, KoSByteModel] = Discriminated(KoSByteModel.kosByteCodec)

  object X {
    private val Xkey = KoSByteModel(0xE3.toByte)
    implicit val discriminator: Discriminator[Key, X, KoSByteModel] = Discriminator(Xkey)
  }

  object O {
    private val Okey = KoSByteModel(0xC3.toByte)
    implicit val discriminator: Discriminator[Key, O, KoSByteModel] = Discriminator(Okey)
  }

  implicit class KeyJavaConverter(value: Key) {
    def asJava: JKey = value match {
      case X() => JX
      case O() => JO
    }
  }
}
