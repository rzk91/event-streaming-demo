package com.rzk.util

import com.typesafe.scalalogging.LazyLogging

import scala.util.Try

object SinkStatus extends Enumeration with LazyLogging {
  type SinkStatus = Value
  val ACTIVE, DEBUG, TESTING, DISABLED = Value

  implicit class SinkStatusOps(val status: String) extends AnyVal {
    def toSinkStatus: SinkStatus = Try(withName(status.toUpperCase)).toOption.getOrElse(DISABLED)
  }
}
