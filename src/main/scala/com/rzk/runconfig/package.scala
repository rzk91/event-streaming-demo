package com.rzk

import com.rzk.util.ExecutionEnvironment._
import com.rzk.util.SinkStatus._
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.streaming.api.CheckpointingMode
import pureconfig._
import pureconfig.error.CannotConvert

package object runconfig {

  private[runconfig] val source: ConfigObjectSource = ConfigSource
    .resources("local.conf")
    .optional
    .withFallback(ConfigSource.resources("default.conf"))

  implicit val sinkStatusReader: ConfigReader[SinkStatus] = ConfigReader[String].map(_.toSinkStatus)

  implicit val environmentReader: ConfigReader[Env] = ConfigReader[String].map(_.toEnv.getOrElse(LOCAL))

  implicit val checkpointModeReader: ConfigReader[CheckpointingMode] = ConfigReader[String].emap { mode =>
    mode.toUpperCase match {
      case "EXACTLY_ONCE"  => Right(CheckpointingMode.EXACTLY_ONCE)
      case "AT_LEAST_ONCE" => Right(CheckpointingMode.AT_LEAST_ONCE)
      case _ =>
        Left(
          CannotConvert(mode, "CheckpointingMode", "Only available options are: 'EXACTLY_ONCE' and 'AT_LEAST_ONCE'")
        )
    }
  }

  implicit val deliveryGuaranteeReader: ConfigReader[DeliveryGuarantee] = ConfigReader[String].emap { mode =>
    mode.toUpperCase match {
      case "EXACTLY_ONCE"  => Right(DeliveryGuarantee.EXACTLY_ONCE)
      case "AT_LEAST_ONCE" => Right(DeliveryGuarantee.AT_LEAST_ONCE)
      case "NONE"          => Right(DeliveryGuarantee.NONE)
      case _ =>
        Left(
          CannotConvert(
            mode,
            "DeliveryGuarantee",
            "Only available options are: 'EXACTLY_ONCE', 'AT_LEAST_ONCE', and 'NONE'"
          )
        )
    }
  }
}
