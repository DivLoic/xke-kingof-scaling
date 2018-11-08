package fr.xebia.ldi.common.frame

import fr.xebia.ldi.common.frame.KoSByteModel.kosByteCodec
import fr.xebia.ldi.common.schema.ImpactType.{Critical => JCritical, Direct => JDirect, Fatal => JFatal, Missed => JMissed, Special => JSpecial}
import fr.xebia.ldi.common.schema.{ImpactType => JImpactType}
import scodec.Codec
import scodec.codecs._

/**
  * Created by loicmdivad.
  */
sealed trait Impact

object Impact {

  case class Fatal(violence: Short) extends Impact
  case class Critical(violence: Short) extends Impact
  case class Special(violence: Short) extends Impact
  case class Direct(violence: Short) extends Impact
  case class Missed() extends Impact

  implicit val discriminated: Discriminated[Impact, KoSByteModel] = Discriminated(kosByteCodec)

  object Fatal {
    val Fatal = KoSByteModel(0x72)
    implicit val discriminator: Discriminator[Impact, Fatal, KoSByteModel] = Discriminator(Fatal)
    implicit val codec: Codec[Fatal] = ushort8.as[Fatal]
  }

  object Critical {
    val Critical = KoSByteModel(0x4F.toByte)
    implicit val discriminator: Discriminator[Impact, Critical, KoSByteModel] = Discriminator(Critical)
    implicit val codec: Codec[Critical] = ushort8.as[Critical]
  }

  object Special {
    val Special = KoSByteModel(0xA2.toByte)
    implicit val discriminator: Discriminator[Impact, Special, KoSByteModel] = Discriminator(Special)
    implicit val codec: Codec[Special] = ushort8.as[Special]
  }

  object Direct {
    val Direct = KoSByteModel(0xB1.toByte)
    implicit val discriminator: Discriminator[Impact, Direct, KoSByteModel] = Discriminator(Direct)
    implicit val codec: Codec[Direct] = ushort8.as[Direct]
  }

  object Missed {
    val Missed = KoSByteModel(0x1F.toByte)
    implicit val discriminator: Discriminator[Impact, Missed, KoSByteModel] = Discriminator(Missed)
  }

  implicit class ImpactJavaConverter(value: Impact) {

    def asJava: Int = value match {
      case Fatal(v) => v.toInt
      case Critical(v) => v.toInt
      case Special(v) => v.toInt
      case Direct(v) => v.toInt
      case Missed() => 0
    }

    def asJavaType: JImpactType = value match {
      case Fatal(_) => JFatal
      case Critical(_) => JCritical
      case Special(_) => JSpecial
      case Direct(_) => JDirect
      case Missed() => JMissed
    }
  }
}
