package com.rzk.fss

import cats.effect._
import com.rzk.bots.thresholdmonitor.{ThresholdMonitorBase, ThresholdMonitorConfig, ThresholdMonitorConfigParser}
import com.rzk.common.{Event, Finding, SensorReading}
import com.rzk.util.implicits.enrichOption
import fs2.Stream

object ThresholdMonitor extends Fs2AnalyticsBase[IO] with ThresholdMonitorBase with IOApp {

  override val botName: String = "threshold-monitor-fs2"

  def analyzeEvents(
    eventStream: Stream[IO, CommittableValue[IO, Event]],
    botConfig: ThresholdMonitorConfig
  ): Stream[IO, CommittableValue[IO, Finding]] =
    eventStream
      .flatMap { committableEvent =>
        SensorReading(committableEvent.value, botConfig)
          .map(CommittableValue(committableEvent.offset, _))
          .toFs2Stream
      }
      .debug(formatter = _.debug, logger = logger.trace(_))
      .flatMap { committableReading =>
        generateFindings(committableReading.value, botConfig)
          .map(CommittableValue(committableReading.offset, _))
          .toFs2Stream
      }
      .debug(formatter = _.debug, logger = logger.debug(_))

  override def run(args: List[String]): IO[ExitCode] =
    runAnalysis(ThresholdMonitorConfigParser(args)).as(ExitCode.Success)
}
