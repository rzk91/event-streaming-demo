package com.rzk.bots.config

trait BotConfig {
  def deviceId: String
  def sensor: String
}

object BotConfig {
  val validParameters: Set[String] = Set("deviceId", "sensor")
}
