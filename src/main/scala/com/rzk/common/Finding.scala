package com.rzk.common

import com.rzk.util.Severity.Severity
import io.circe._
import io.circe.generic.semiauto._
import org.apache.flink.api.common.typeinfo.TypeInformation

case class Finding(
  botName: String,
  deviceId: String,
  selector: String,
  start: Long,
  end: Long,
  severity: Severity,
  title: String,
  description: String
)

object Finding {
  implicit lazy val findingTypeInfo: TypeInformation[Finding] = TypeInformation.of(classOf[Finding])
  implicit val decodeFinding: Decoder[Finding] = deriveDecoder
  implicit val encodeFinding: Encoder[Finding] = deriveEncoder
}
