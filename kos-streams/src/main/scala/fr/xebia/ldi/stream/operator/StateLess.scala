package fr.xebia.ldi.stream.operator

import java.lang.Iterable

import fr.xebia.ldi.common.frame.Hit
import fr.xebia.ldi.common.schema.{FrameBody, Hit => JHit}
import org.apache.kafka.streams.kstream.ValueMapper

import scala.collection.JavaConverters._

/**
  * Created by loicmdivad.
  */
object StateLess {

  val flatMapDecoder: ValueMapper[FrameBody, Iterable[JHit]] = (value: FrameBody) => value
    .getBytes
    .asScala
    .map(Hit.decode)
    .filter(_.isDefined)
    .map(_.get.value.asJava)
    .asJava
}
