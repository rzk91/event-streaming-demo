package com.rzk.bots.config

import cats.data.Validated
import cats.instances.map._
import cats.instances.vector._
import cats.syntax.semigroup._

import scala.annotation.tailrec
import scala.util.matching.Regex

abstract class ConfigParser[Config <: BotConfig](val args: List[String], validParams: Set[String]) {

  type ParserResult = Validated[Vector[String], Map[String, String]]
  type ConfigResult = Validated[Vector[String], Config]

  final val ValidParameterRegex: Regex = """(^--(\w*))""".r
  final val config: ConfigResult = rawConfig.andThen(parser).leftMap(_ :+ usage)

  /** This method should read the parameter map in `ParserResult` and produce a Validated instance with either:
    *  - A vector of errors (based on `usage`), or
    *  - the final config
    * @return `ConfigResult`, which is a Validated instance of errors or config
    */
  protected def parser(paramMap: Map[String, String]): ConfigResult

  /** This method/value should contain a description of the available parameters and how they must be passed
    * @return A string with a description of passable parameters
    */
  protected def usage: String

  final protected def rawConfig: ParserResult = {
    @tailrec
    def loop(list: List[String], acc: ParserResult): ParserResult = list match {
      case Nil => acc
      case ValidParameterRegex(_, param) :: value :: tail if validParams(param) =>
        loop(tail, acc.map(_ + (param -> value)))
      case ValidParameterRegex(_, param) :: _ :: tail =>
        loop(tail, acc |+| Validated.invalid(Vector(s"Unknown argument: $param")))
      case _ =>
        acc |+| Validated.invalid(Vector("Invalid format of input arguments; please refer to usage below"))
    }

    if (args.isEmpty) Validated.invalid(Vector(usage)) else loop(args, Validated.valid(Map.empty))
  }

  final protected def getParameterValue(
    map: Map[String, String],
    key: String,
    ifMissing: => String
  ): Validated[Vector[String], String] = Validated.fromOption(map.get(key), Vector(ifMissing))
}
