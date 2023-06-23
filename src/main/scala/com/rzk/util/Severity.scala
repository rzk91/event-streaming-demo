package com.rzk.util

import io.circe.{Decoder, Encoder}

import scala.util.Try

object Severity extends Enumeration {
  type Severity = Value
  val INFO, LOW, MEDIUM, HIGH = Value

  implicit class SeverityOps(private val severity: String) extends AnyVal {
    def toSeverity: Severity = Try(withName(severity.toUpperCase)).getOrElse(INFO)
  }

  implicit val severityEncoder: Encoder[Severity] = Encoder.encodeEnumeration(Severity)
  implicit val severityDecoder: Decoder[Severity] = Decoder.decodeEnumeration(Severity)
}
