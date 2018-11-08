package fr.xebia.ldi.common.frame

import fr.xebia.ldi.common.frame.Direction.{Down, Left, Right, Up}
import fr.xebia.ldi.common.frame.Game.{HeatofBattleFromParadise, HowlingBlood, MaximumImpact, NeoBlood, Neowave}
import fr.xebia.ldi.common.frame.Impact.{Critical, Direct, Fatal, Missed, Special}
import fr.xebia.ldi.common.frame.Key.{O, X}
import fr.xebia.ldi.common.frame.Level.{Inter, NewBie, Pro}
import fr.xebia.ldi.common.schema.{ImpactType, Direction => JDirection, Game => JGame, Hit => JHit, Key => JKey, Level => JLevel}
import scodec.bits._
import scodec.{Codec, DecodeResult}
import scodec.codecs._
import scodec.codecs.implicits._

/**
  * Created by loicmdivad.
  */
case class Hit(key: Key, direction: Option[Direction], impact: Impact, doubleKey: Option[Key] = None, level: Level, game: Game) {

  def asJava: JHit = new JHit(
    impact.asJava,
    key.asJava,
    doubleKey.map(_.asJava).orNull,
    direction.map(_.asJava).orNull,
    impact.asJavaType,
    level.asJava,
    game.asJava
  )
}

object Hit {

  def apply(hit: JHit): Hit = new Hit(

    hit.getKey match {
      case JKey.X => X()
      case JKey.O => O()
    },

    Option(hit.getDirection).map {
      case JDirection.UP => Up()
      case JDirection.DOWN => Down()
      case JDirection.LEFT => Left()
      case JDirection.RIGHT => Right()
    },

    hit.getImpactType match {
      case ImpactType.Fatal => Fatal(hit.getImpact.shortValue())
      case ImpactType.Direct => Direct(hit.getImpact.shortValue())
      case ImpactType.Special => Special(hit.getImpact.shortValue())
      case ImpactType.Critical => Critical(hit.getImpact.shortValue())
      case ImpactType.Missed => Missed()
    },

    Option(hit.getDoubleKey).map {
      case JKey.X => X()
      case JKey.O => O()
    },

    hit.getLevel match {
      case JLevel.Pro => Pro()
      case JLevel.Inter => Inter()
      case JLevel.NewBie => NewBie()
    },

    hit.getGame match {
      case JGame.Neowave => Neowave()
      case JGame.NeoBlood => NeoBlood()
      case JGame.HowlingBlood => HowlingBlood()
      case JGame.MaximumImpact => MaximumImpact()
      case JGame.HeatofBattleFromParadise => HeatofBattleFromParadise()
    }
  )

  def decode(hexaHit: String): Option[DecodeResult[Hit]] =
    ByteVector.fromHex(hexaHit).flatMap(v => Codec.decode[Hit](v.bits).toOption)
}
