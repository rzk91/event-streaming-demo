package com.rzk.flink

import com.typesafe.scalalogging.Logger
import org.apache.flink.streaming.api.functions.sink.SinkFunction

class LoggerSink[IN](
  logger: Logger,
  loggingLevel: String = "error",
  formatter: IN => String = (in: IN) => in.toString
) extends SinkFunction[IN] {

  private lazy val log: String => Unit = {
    val level = loggingLevel.toLowerCase
    val validSet = Set("error", "warn", "info", "debug", "trace")
    if (!validSet(level)) logger.warn(s"Unknown logging level: $loggingLevel. Using INFO instead.")
    (s: String) =>
      level match {
        case "error" => logger.error(s)
        case "warn"  => logger.warn(s)
        case "debug" => logger.debug(s)
        case "trace" => logger.trace(s)
        case _       => logger.info(s)
      }
  }

  override def invoke(value: IN, context: SinkFunction.Context): Unit = log(formatter(value))
}
