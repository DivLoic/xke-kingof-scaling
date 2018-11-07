package fr.xebia.ldi.stream.operator

import fr.xebia.ldi.common.schema.{FrameBody, FrameHeader, GameSession}
import org.apache.kafka.streams.kstream.{Aggregator, Initializer, ValueJoiner}
import fr.xebia.ldi.common.schema.{Hit => JHit}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/**
  * Created by loicmdivad.
  */
object StateFull {

  val joinWindowSize: FiniteDuration = 3 seconds

  // TODO: Add extra information in the response
  val reqResponseJoiner: ValueJoiner[FrameBody, FrameBody, FrameBody] = (rqBody: FrameBody, _: FrameBody) => rqBody

  val sessionInitializer: Initializer[GameSession] = () => new GameSession(Vector.empty[JHit].asJava)

  val sessionAggregator: Aggregator[FrameHeader, JHit, GameSession] =
    (key: FrameHeader, value: JHit, aggregate: GameSession) =>
      new GameSession((aggregate.getHits.asScala :+ value).asJava)

}