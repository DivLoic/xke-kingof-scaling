package fr.xebia.ldi.common.frame

import scodec.{Attempt, Codec, DecodeResult, SizeBound}
import scodec.bits.BitVector
import scodec.codecs._


/**
  * Created by loicmdivad.
  */
case class ByteIndex(byte: Byte) extends AnyVal


object ByteIndex {

  implicit val codec: Codec[ByteIndex] = new Codec[ByteIndex] {

    override def encode(value: ByteIndex): Attempt[BitVector] = byte.encode(value.byte)

    override def sizeBound: SizeBound = ???

    override def decode(bits: BitVector): Attempt[DecodeResult[ByteIndex]] = byte.decode(bits).map { result =>
      result.map(ByteIndex(_))
    }
  }

}