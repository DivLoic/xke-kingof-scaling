package fr.xebia.ldi.common.frame


/**
  * Created by loicmdivad.
  */
class Frame {

}

object Frame extends App {

  import scodec.{Codec, _}
  import scodec.bits._
  import scodec.codecs._
  import scodec.codecs.implicits._

  import fr.xebia.ldi.common.frame.Key.{X, O}
  import fr.xebia.ldi.common.frame.Direction.{Down, Left, Right, Up}
  import fr.xebia.ldi.common.frame.Impact.{Critical, Direct, Fatal, Missed, Special}

  import fr.xebia.ldi.common.frame.Direction.discriminated
  import fr.xebia.ldi.common.frame.Direction.Down.discriminator
  import fr.xebia.ldi.common.frame.Direction.Up.discriminator
  import fr.xebia.ldi.common.frame.Direction.Right.discriminator
  import fr.xebia.ldi.common.frame.Direction.Left.discriminator

  import fr.xebia.ldi.common.frame.Impact.discriminated
  import fr.xebia.ldi.common.frame.Impact.Fatal._
  import fr.xebia.ldi.common.frame.Impact.Critical._
  import fr.xebia.ldi.common.frame.Impact.Special._
  import fr.xebia.ldi.common.frame.Impact.Missed._
  import fr.xebia.ldi.common.frame.Impact.Direct._

  import fr.xebia.ldi.common.frame.Key.discriminated
  import fr.xebia.ldi.common.frame.Key.X.discriminator
  import fr.xebia.ldi.common.frame.Key.O.discriminator


  println(Codec.encode(Hit(X(), None, Missed(), None)).require.toHex)
  println(Codec.encode(Hit(O(), Some(Up()), Direct(10), Some(X()))).require.toHex)
  println(Codec.encode(Hit(O(), Some(Down()), Special(2), None)).require.toHex)
  println(Codec.encode(Hit(O(), Some(Left()), Critical(20), Some(O()))).require.toHex)
  println(Codec.encode(Hit(O(), Some(Right()), Fatal(100), None)).require.toHex)

  println(Codec.decode[Hit](hex"e3001f00".bits))

  println(Codec.decode[Hit](hex"c3ff00b10affe3".bits))

  sealed trait Foo
  case class Bar(bar: Short, mut: Int) extends Foo


  object Foo {
    implicit val discriminated: Discriminated[Foo, Int] = Discriminated(uint8)
  }

  object Bar {
    implicit val discriminator: Discriminator[Foo, Bar, Int] = Discriminator(1)
    implicit val codec: Codec[Bar] = (ushort8 :: uint8).as[Bar]
  }

}