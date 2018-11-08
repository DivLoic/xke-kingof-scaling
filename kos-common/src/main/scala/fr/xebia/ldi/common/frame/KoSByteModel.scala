package fr.xebia.ldi.common.frame

import scodec.{Attempt, Codec, DecodeResult, SizeBound}
import scodec.bits.BitVector
import scodec.codecs._


/**
  * Created by loicmdivad.
  */
case class KoSByteModel(byte: Byte) extends AnyVal


object KoSByteModel {

  implicit val kosByteCodec: Codec[KoSByteModel] = new Codec[KoSByteModel] {

    override def encode(value: KoSByteModel): Attempt[BitVector] = byte.encode(value.byte)

    override def sizeBound: SizeBound = byte.sizeBound

    override def decode(bits: BitVector): Attempt[DecodeResult[KoSByteModel]] = byte.decode(bits).map { result =>
      result.map(KoSByteModel(_))
    }
  }
}