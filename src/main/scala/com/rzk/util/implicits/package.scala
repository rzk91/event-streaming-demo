package com.rzk.util

import com.ariskk.flink4s.DataStream
import com.rzk.util.implicits.TimeUtil.{RichStringTime, RichTime, RichTimeUnit}
import io.circe.Json

import java.util.Properties
import scala.concurrent.duration.{Duration, FiniteDuration}

package object implicits {

  @inline implicit def iterableToRichIterable[A](l: Iterable[A]): RichIterable[A] = new RichIterable(l)
  @inline implicit def arrayToRichIterable[A](l: Array[A]): RichIterable[A] = new RichIterable(l)
  @inline implicit def stringToRichIterable(s: String): RichIterable[Char] = new RichIterable(s)
  @inline implicit def enrichOption[A](opt: Option[A]): RichOption[A] = new RichOption(opt)
  @inline implicit def enrichMap[K, V](map: Map[K, V]): RichMap[K, V] = new RichMap(map)
  @inline implicit def enrichString(str: String): RichString = new RichString(str)
  @inline implicit def enrichJson(json: Json): RichJson = new RichJson(json)
  @inline implicit def enrichDouble(x: Double): RichDouble = new RichDouble(x)
  @inline implicit def enrichProperties(prop: Properties): RichProperty = new RichProperty(prop)
  @inline implicit def enrichTime(t: Long): RichTime = new RichTime(t)
  @inline implicit def enrichStringTime(s: String): RichStringTime = new RichStringTime(s)
  @inline implicit def enrichTimeUnit(s: String): RichTimeUnit = new RichTimeUnit(s)
  @inline implicit def enrichDataStream[A](stream: DataStream[A]): RichDataStream[A] = new RichDataStream(stream)

  implicit def durationToFiniteDuration(d: Duration): FiniteDuration =
    FiniteDuration(if (d.isFinite) d.length else Int.MaxValue, d.unit)
}
