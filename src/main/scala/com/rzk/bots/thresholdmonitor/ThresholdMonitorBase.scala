package com.rzk.bots.thresholdmonitor

import com.rzk.common.{AnalyticsBase, Finding, SensorReading}
import com.rzk.util.Severity

trait ThresholdMonitorBase { _: AnalyticsBase =>

  override type Config = ThresholdMonitorConfig
  override type Parser = ThresholdMonitorConfigParser

  def generateFindings(reading: SensorReading, config: ThresholdMonitorConfig): Option[Finding] = reading match {
    case SensorReading(timestamp, deviceId, sensor, value) if config.thresholdExceeded(value) =>
      Some(
        Finding(
          botName = botName,
          deviceId = deviceId,
          selector = sensor,
          start = timestamp,
          end = timestamp,
          severity = Severity.HIGH,
          title = s"Threshold Exceeded at Sensor '$sensor'",
          description =
            s"$sensor value $value ${if (config.upper) "above" else "below"} configured threshold ${config.threshold}"
        )
      )
    case _ => None
  }
}
