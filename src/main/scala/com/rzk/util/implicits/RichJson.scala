package com.rzk.util.implicits

import io.circe._
import io.circe.optics.JsonPath
import io.circe.optics.JsonPath._
import org.apache.commons.lang3.StringUtils.{endsWith, equalsIgnoreCase, startsWith}

import java.time.ZoneId

class RichJson(private val j: Json) extends AnyVal {

  // Getter methods
  def stringOption(key: => String, path: Option[JsonPath] = None): Option[String] =
    path.fold(_root(key))(_.selectDynamic(key)).string.getOption(j)

  def nonEmptyStringOption(key: => String, path: Option[JsonPath] = None): Option[String] =
    stringOption(key, path).filterNot(_.isEmpty)

  def string(key: => String, default: => String, path: Option[JsonPath] = None): String =
    stringOption(key, path).getOrElse(default)

  def nonEmptyString(key: => String, default: => String, path: Option[JsonPath] = None): String =
    nonEmptyStringOption(key, path).getOrElse(default)

  def zoneIdOption(key: => String, path: Option[JsonPath] = None): Option[ZoneId] =
    stringOption(key, path).flatMap(_.zoneIdOption)

  def zoneId(key: => String, default: => ZoneId, path: Option[JsonPath] = None): ZoneId =
    zoneIdOption(key, path).getOrElse(default)

  def numberOption(key: => String, path: Option[JsonPath] = None): Option[Double] =
    path
      .fold(_root(key))(_.selectDynamic(key))
      .number
      .getOption(j)
      .map(_.toDouble)
      .orElse(stringOption(key, path).flatMap(_.asDouble))

  def number(key: => String, default: => Double, path: Option[JsonPath] = None): Double =
    numberOption(key, path).getOrElse(default)

  def intOption(key: => String, path: Option[JsonPath] = None): Option[Int] =
    numberOption(key, path).map(_.toInt)

  def int(key: => String, default: => Int, path: Option[JsonPath] = None): Int =
    intOption(key, path).getOrElse(default)

  def longOption(key: => String, path: Option[JsonPath] = None): Option[Long] =
    numberOption(key, path).map(_.toLong)

  def long(key: => String, default: => Long, path: Option[JsonPath] = None): Long =
    longOption(key, path).getOrElse(default)

  def doubleOption(key: => String, path: Option[JsonPath] = None): Option[Double] =
    numberOption(key, path)

  def double(key: => String, default: => Double, path: Option[JsonPath] = None): Double =
    doubleOption(key, path).getOrElse(default)

  def booleanOption(key: => String, path: Option[JsonPath] = None): Option[Boolean] =
    path
      .fold(_root(key))(_.selectDynamic(key))
      .boolean
      .getOption(j)
      .orElse(stringOption(key, path).flatMap(_.asBoolean))

  def boolean(key: => String, default: => Boolean, path: Option[JsonPath] = None): Boolean =
    booleanOption(key, path).getOrElse(default)

  def stringSetOption(key: => String, path: Option[JsonPath] = None): Option[Set[String]] =
    path
      .fold(_root(key))(_.selectDynamic(key))
      .arr
      .getOption(j)
      .map(_.flatMap(_.asString.map(_.trim)).toSet)

  def stringSet(
    key: => String,
    default: => Set[String],
    path: Option[JsonPath] = None
  ): Set[String] =
    stringSetOption(key, path).getOrElse(default)

  def jsonOption(key: => String, path: Option[JsonPath] = None): Option[Json] =
    path.fold(_root(key))(_.selectDynamic(key)).json.getOption(j)

  def json(key: => String, default: => Json, path: Option[JsonPath] = None): Json =
    jsonOption(key, path).getOrElse(default)

  def jsonListOption(key: => String, path: Option[JsonPath] = None): Option[List[Json]] = {
    val maybeJson = jsonOption(key, path)
    maybeJson.flatMap(_.asArray.map(_.toList)).orElse(maybeJson.map(List(_)))
  }

  def jsonList(
    key: => String,
    default: => List[Json],
    path: Option[JsonPath] = None
  ): List[Json] = jsonListOption(key, path).getOrElse(default)

  def jsonSetOption(key: => String, path: Option[JsonPath] = None): Option[Set[Json]] =
    jsonListOption(key, path).map(_.toSet)

  def jsonSet(key: => String, default: => Set[Json], path: Option[JsonPath] = None): Set[Json] =
    jsonSetOption(key, path).getOrElse(default)

  def genericOption[A: Decoder: Encoder](key: => String, path: Option[JsonPath] = None): Option[A] =
    path.fold(_root(key))(_.selectDynamic(key)).as[A].getOption(j)

  def generic[A: Decoder: Encoder](key: => String, default: => A, path: Option[JsonPath] = None): A =
    genericOption(key, path).getOrElse(default)

  // Comparison methods
  def stringEquals(key: => String, value: => String, path: Option[JsonPath] = None): Boolean =
    compareUsing(key, value, path)(stringOption, _ == _)

  def stringEqualsIgnoreCase(
    key: => String,
    value: => String,
    path: Option[JsonPath] = None
  ): Boolean = compareUsing(key, value, path)(stringOption, equalsIgnoreCase)

  def stringStartsWith(key: => String, value: => String, path: Option[JsonPath] = None): Boolean =
    compareUsing(key, value, path)(stringOption, startsWith)

  def stringEndsWith(key: => String, value: => String, path: Option[JsonPath] = None): Boolean =
    compareUsing(key, value, path)(stringOption, endsWith)

  def numberEquals(key: => String, value: => Double, path: Option[JsonPath] = None): Boolean =
    compareUsing(key, value, path)(numberOption, _ == _)

  def booleanEquals(key: => String, value: => Boolean, path: Option[JsonPath] = None): Boolean =
    compareUsing(key, value, path)(booleanOption, _ == _)

  def numberGreaterThan(
    key: => String,
    value: => Double,
    path: Option[JsonPath] = None
  ): Boolean = compareUsing(key, value, path)(numberOption, _ > _)

  def numberLessThan(key: => String, value: => Double, path: Option[JsonPath] = None): Boolean =
    compareUsing(key, value, path)(numberOption, _ < _)

  def numberGreaterThanOrEqualTo(
    key: => String,
    value: => Double,
    path: Option[JsonPath] = None
  ): Boolean = compareUsing(key, value, path)(numberOption, _ >= _)

  def numberLessThanOrEqualTo(
    key: => String,
    value: => Double,
    path: Option[JsonPath] = None
  ): Boolean = compareUsing(key, value, path)(numberOption, _ <= _)

  // Other utility methods
  def isEmpty: Boolean = j.isNull
  def isDefined: Boolean = !isEmpty
  def nonEmpty: Boolean = isDefined
  def keys: Iterable[String] = j.asObject.toList.flatMap(_.keys)
  def keySet: Set[String] = keys.toSet
  def -(key: => String): Json = j.mapObject(_.remove(key))
  def +(field: (String, Json)): Json = j.mapObject(_.add(field._1, field._2))
  def asOption: Option[Json] = if (isEmpty) None else Some(j)
  def asList: List[Json] = j.asArray.fold(List(j))(_.toList)
  def deepMergeOption(maybeThat: Option[Json]): Json = maybeThat.fold(j)(j.deepMerge)

  private def _root(key: => String): JsonPath = root.selectDynamic(key)

  private def compareUsing[A](key: => String, value: => A, path: Option[JsonPath] = None)(
    getFn: (=> String, Option[JsonPath]) => Option[A],
    compareFn: (A, A) => Boolean
  ): Boolean = getFn(key, path).exists(compareFn(_, value))
}
