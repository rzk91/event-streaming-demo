package com.rzk.kstreams

import com.rzk.bots.thresholdmonitor.{ThresholdMonitorBase, ThresholdMonitorConfig, ThresholdMonitorConfigParser}
import com.rzk.common.{Event, Finding, SensorReading}
import com.rzk.runconfig.RunConfiguration.kafkaConfig
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde

object ThresholdMonitor extends KStreamsAnalyticsBase[String, Event, String, Finding] with ThresholdMonitorBase {

  val botName: String = "threshold-monitor-kstream"

  def main(args: Array[String]): Unit =
    analyze(ThresholdMonitorConfigParser(args), kafkaConfig.topics.events, kafkaConfig.topics.findings)

  def analyzeEvents(stream: KStream[String, Event], config: ThresholdMonitorConfig): KStream[String, Finding] =
    stream
      .peek { case (_, event) => logger.trace(s"$event") }
      .flatMapValues(SensorReading(_, config))
      .peek { case (_, reading) => logger.trace(s"$reading") }
      .flatMapValues(generateFindings(_, config))
      .peek { case (_, finding) => logger.debug(s"$finding") }
}
