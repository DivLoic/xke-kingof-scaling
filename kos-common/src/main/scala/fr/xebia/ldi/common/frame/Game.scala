package fr.xebia.ldi.common.frame

import scodec.codecs.{Discriminated, Discriminator}
import fr.xebia.ldi.common.schema.{Game => JGame}
import fr.xebia.ldi.common.schema.Game.{
  Neowave => JNeowave,
  NeoBlood => JNeoBlood,
  HowlingBlood => JHowlingBlood,
  MaximumImpact => JMaximumImpact,
  HeatofBattleFromParadise  => JHeatofBattleFromParadise
}


/**
  * Created by loicmdivad.
  */
sealed trait Game

object Game {

  case class Neowave() extends Game

  case class NeoBlood() extends Game

  case class HowlingBlood() extends Game

  case class MaximumImpact() extends Game

  case class HeatofBattleFromParadise() extends Game

  implicit val discriminated: Discriminated[Game, KoSByteModel] = Discriminated(KoSByteModel.kosByteCodec)

  object Neowave {
    val neowaveByte = KoSByteModel(0xA5.toByte)
    implicit val discriminator: Discriminator[Game, Neowave, KoSByteModel] = Discriminator(neowaveByte)
  }

  object NeoBlood {
    val neoBloodByte = KoSByteModel(0xB5.toByte)
    implicit val discriminator: Discriminator[Game, NeoBlood, KoSByteModel] = Discriminator(neoBloodByte)
  }

  object HowlingBlood {
    val howlingBloodByte = KoSByteModel(0xC5.toByte)
    implicit val discriminator: Discriminator[Game, HowlingBlood, KoSByteModel] = Discriminator(howlingBloodByte)
  }

  object MaximumImpact {
    val maximumImpactByte = KoSByteModel(0xD5.toByte)
    implicit val discriminator: Discriminator[Game, MaximumImpact, KoSByteModel] = Discriminator(maximumImpactByte)
  }

  object HeatofBattleFromParadise {
    val heatofBattleFromParadiseByte = KoSByteModel(0xE5.toByte)
    implicit val discriminator: Discriminator[Game, HeatofBattleFromParadise, KoSByteModel] = Discriminator(heatofBattleFromParadiseByte)
  }

  implicit class GameScalaConverter(value: JGame) {
    def asScala: Game = value match {
      case JNeowave => Neowave()
      case JNeoBlood => NeoBlood()
      case JHowlingBlood => HowlingBlood()
      case JMaximumImpact => MaximumImpact()
      case JHeatofBattleFromParadise => HeatofBattleFromParadise()
    }
  }

  implicit class GameJavaConverter(value: Game) {
    def asJava: JGame = value match {
      case Neowave() => JNeowave
      case NeoBlood() => JNeoBlood
      case HowlingBlood() => JHowlingBlood
      case MaximumImpact() => JMaximumImpact
      case HeatofBattleFromParadise() => JHeatofBattleFromParadise
    }
  }
}
