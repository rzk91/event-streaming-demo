package com.rzk.bots.thresholdmonitor

import cats.data.Validated
import cats.syntax.apply._
import com.rzk.bots.config.ConfigParser
import com.rzk.util.implicits.enrichString

class ThresholdMonitorConfigParser(args: List[String])
    extends ConfigParser[ThresholdMonitorConfig](args, ThresholdMonitorConfig.validParameters) {

  override def parser(parameters: Map[String, String]): ConfigResult = {
    val deviceId = getParameterValue(parameters, "deviceId", "key 'deviceId' is missing")
    val sensor = getParameterValue(parameters, "sensor", "key 'sensor' is missing")
    val threshold = getParameterValue(parameters, "threshold", "key 'threshold' is missing").andThen { t =>
      Validated.fromOption(t.asDouble, Vector("Parameter 'threshold' must be a number"))
    }
    val upper = getParameterValue(parameters, "upper", "key 'upper' is missing").andThen { u =>
      Validated.fromOption(u.asBoolean, Vector("Parameter 'upper' must be boolean"))
    }
    val lower = getParameterValue(parameters, "lower", "key 'lower' is missing").andThen { l =>
      Validated.fromOption(l.asBoolean, Vector("Parameter 'lower' must be boolean"))
    }

    val upperOrLower = upper
      .orElse(lower.map(!_))
      .orElse(Validated.invalid(Vector("One of the keys 'upper' or 'lower' must be present")))

    (deviceId, sensor, threshold, upperOrLower).mapN(ThresholdMonitorConfig.apply)
  }

  override protected val usage: String =
    """
      | Mandatory parameters:
      | --deviceId : Device ID to be considered, as string
      | --sensor   : Sensor name to be considered, as string
      | --threshold: Threshold value against which incoming sensor values are compared, as double
      |
      | Optionally mandatory parameters (one of the following):
      | --upper    : upper/positive threshold, as boolean
      | --lower    : lower/negative threshold, as boolean
      |""".stripMargin.trim
}

object ThresholdMonitorConfigParser {
  def apply(args: List[String]): ThresholdMonitorConfigParser = new ThresholdMonitorConfigParser(args)
  def apply(args: Array[String]): ThresholdMonitorConfigParser = new ThresholdMonitorConfigParser(args.toList)
}
