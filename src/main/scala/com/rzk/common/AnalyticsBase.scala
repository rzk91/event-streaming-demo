package com.rzk.common

import cats.effect.Sync
import com.rzk.bots.config.{BotConfig, ConfigParser}
import com.typesafe.scalalogging.LazyLogging

trait AnalyticsBase extends Serializable with LazyLogging {

  type Config <: BotConfig
  type Parser <: ConfigParser[Config]

  def botName: String

  final protected def readConfig(parser: Parser): Config = {
    val config = parser.config.valueOr { errorList =>
      val exception = s"${errorList.map("* " + _).mkString("Exception in passed arguments: \n", "\n", "")}"
      logger.error(exception)
      throw new IllegalArgumentException(s"Invalid arguments in ${parser.args}")
    }

    logger.info(s"Running $botName with following parameters: $config")

    config
  }

  final protected def readConfigF[F[_]](parser: Parser)(implicit F: Sync[F]): F[Config] =
    F.delay(readConfig(parser))
}
