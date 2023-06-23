package com.rzk.common

import cats.kernel.{Eq, Monoid}
import com.rzk.util.implicits.enrichJson
import io.circe._
import io.circe.syntax._
import org.apache.flink.api.common.typeinfo.TypeInformation

case class Event(
  timestamp: Long,
  deviceId: String,
  content: Json
) extends TimestampedObject {
  def contentKeys: Set[String] = content.keySet
  def boolField(key: String): Option[Boolean] = content.booleanOption(key)
  def boolField(key: String, default: => Boolean): Boolean = content.boolean(key, default)
  def intField(key: String): Option[Int] = content.intOption(key)
  def intField(key: String, default: => Int): Int = content.int(key, default)
  def longField(key: String): Option[Long] = content.longOption(key)
  def longField(key: String, default: => Long): Long = content.long(key, default)
  def doubleField(key: String): Option[Double] = content.doubleOption(key)
  def doubleField(key: String, default: => Double): Double = content.double(key, default)
  def stringField(key: String): Option[String] = content.stringOption(key)
  def stringField(key: String, default: => String): String = content.string(key, default)

  def field[A: Decoder: Encoder](key: String): Option[A] = content.genericOption(key)
  def field[A: Decoder: Encoder](key: String, default: => A): A = field(key).getOrElse(default)

  // Transformations
  def filter(p: Event => Boolean): Option[Event] = Some(this).filter(p)
  def map[A](f: Event => A): A = f(this)
  def collect[A](pf: PartialFunction[Event, A]): Option[A] = pf.lift(this)

  override def toString: String = s"Event($timestamp, $deviceId, ${content.noSpaces})"
}

object Event {

  // Type class instances and other implicit values
  implicit val eventEq: Eq[Event] = Eq.instance { (e1, e2) =>
    e1.deviceId == e2.deviceId && e1.timestamp == e2.timestamp && e1.content.equals(e2.content)
  }

  implicit val eventMonoid: Monoid[Event] = Monoid.instance(
    emptyValue = Event(0L, "", Json.Null),
    {
      case (e1, e2) if e1.deviceId == e2.deviceId =>
        Event(math.max(e1.timestamp, e2.timestamp), e1.deviceId, e1.content.deepMerge(e2.content))
      case _ => throw new NoSuchMethodException("Combination of events is only possible for same device IDs")
    }
  )

  implicit val decodeEvent: Decoder[Event] = (c: HCursor) =>
    for {
      timestamp <- c.downField("timestamp").as[Long]
      deviceId  <- c.downField("deviceId").as[String]
      content   <- c.downField("content").as[Json]
    } yield Event(timestamp, deviceId, content)

  implicit val encodeEvent: Encoder[Event] = (e: Event) =>
    Map(
      "timestamp" -> e.timestamp.asJson,
      "deviceId"  -> e.deviceId.asJson,
      "content"   -> e.content.noSpaces.asJson
    ).asJson

  implicit lazy val eventTypeInfo: TypeInformation[Event] = TypeInformation.of(classOf[Event])
}
