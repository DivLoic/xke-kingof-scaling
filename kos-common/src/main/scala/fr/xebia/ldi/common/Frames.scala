package fr.xebia.ldi.common

import fr.xebia.ldi.common.frame.Direction.{Down, Left, Right, Up}
import fr.xebia.ldi.common.frame.Game.NeoBlood
import fr.xebia.ldi.common.frame.Hit
import fr.xebia.ldi.common.frame.Impact.{Critical, Direct, Fatal, Missed, Special}
import fr.xebia.ldi.common.frame.Key.{O, X}
import fr.xebia.ldi.common.frame.Level.NewBie
import scodec.Codec
import scodec.bits._
import scodec.codecs.implicits._


/**
  * Created by loicmdivad.
  */
class Frames {

}

object Frames extends App {

  println(Codec.encode(Hit(X(), None, Missed(), None, NewBie(), NeoBlood())).require.toHex)
  println(Codec.encode(Hit(O(), Some(Up()), Direct(10), Some(X()), NewBie(), NeoBlood())).require.toHex)
  println(Codec.encode(Hit(O(), Some(Down()), Special(2), None, NewBie(), NeoBlood())).require.toHex)
  println(Codec.encode(Hit(O(), Some(Left()), Critical(20), Some(O()), NewBie(), NeoBlood())).require.toHex)
  println(Codec.encode(Hit(O(), Some(Right()), Fatal(100), None, NewBie(), NeoBlood())).require.toHex)

  println(Codec.decode[Hit](hex"e3001f00d9b5".bits))

  println(Codec.decode[Hit](hex"c3ff8d4f14ffc3d9b5".bits))
  println(Codec.decode[Hit](hex"c3ff8ab13000e9c5".bits))
}