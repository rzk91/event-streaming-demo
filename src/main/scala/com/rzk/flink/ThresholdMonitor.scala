package com.rzk.flink

import com.ariskk.flink4s.{DataStream, StreamExecutionEnvironment}
import com.rzk.bots.thresholdmonitor.{ThresholdMonitorBase, ThresholdMonitorConfig, ThresholdMonitorConfigParser}
import com.rzk.common.{Event, Finding, SensorReading}
import com.rzk.util.implicits.enrichDataStream

object ThresholdMonitor extends FlinkAnalyticsBase with ThresholdMonitorBase {

  override val botName: String = "threshold-monitor-flink"

  def main(args: Array[String]): Unit = analyze(ThresholdMonitorConfigParser(args))

  override def analyzeEvents(eventStream: DataStream[Event], config: ThresholdMonitorConfig)(implicit
    env: StreamExecutionEnvironment
  ): DataStream[Finding] =
    eventStream
      .debug(logger = logger.trace(_))
      .flatMap(SensorReading(_, config))
      .debug(logger = logger.trace(_))
      .flatMap(generateFindings(_, config))
      .debug(logger = logger.debug(_))
}
