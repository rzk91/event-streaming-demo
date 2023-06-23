package com.rzk.flink

import com.ariskk.flink4s.{DataStream, StreamExecutionEnvironment}
import com.rzk.runconfig.RunConfiguration._
import com.rzk.util.SinkStatus.{ACTIVE, DEBUG}
import com.rzk.common.{AnalyticsBase, Event, Finding}

trait FlinkAnalyticsBase extends AnalyticsBase {

  def analyzeEvents(eventStream: DataStream[Event], config: Config)(implicit
    env: StreamExecutionEnvironment
  ): DataStream[Finding]

  final protected def analyze(parser: Parser): Unit = {
    val config = readConfig(parser)

    implicit val env: StreamExecutionEnvironment =
      defaultExecutionEnvironment(executionEnvironmentConfig.parallelism, checkpointingConfig)

    val findings =
      analyzeEvents(
        flinkKafkaSource[Event](botName, kafkaConfig, checkpointingConfig.interval.toMillis, runningLocally),
        config
      )

    kafkaConfig.sinkStatus match {
      case ACTIVE => flinkKafkaSink(findings, kafkaConfig)
      case DEBUG  => findings.addSink(new LoggerSink[Finding](logger))
      case _      => // Do nothing
    }

    env.javaEnv.execute(botName)
  }

}
