package com.rzk.bots.thresholdmonitor

import com.rzk.bots.config.BotConfig

case class ThresholdMonitorConfig(
  deviceId: String,
  sensor: String,
  threshold: Double,
  upper: Boolean
) extends BotConfig {
  def thresholdExceeded(value: Double): Boolean = upper && value > threshold || !upper && value < threshold
}

object ThresholdMonitorConfig {
  val validParameters: Set[String] = BotConfig.validParameters ++ Set("threshold", "upper", "lower")

  def apply(fields: (String, String, Double, Boolean)): ThresholdMonitorConfig =
    new ThresholdMonitorConfig(fields._1, fields._2, fields._3, fields._4)
}
