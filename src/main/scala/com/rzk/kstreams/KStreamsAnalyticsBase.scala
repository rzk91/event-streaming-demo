package com.rzk.kstreams

import com.rzk.common.AnalyticsBase
import com.rzk.runconfig.RunConfiguration.{kafkaConfig, runningLocally}
import com.rzk.util.SinkStatus.{ACTIVE, DEBUG}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream

trait KStreamsAnalyticsBase[KIN, VIN, KOUT, VOUT] extends AnalyticsBase {

  def analyzeEvents(eventStream: KStream[KIN, VIN], config: Config): KStream[KOUT, VOUT]

  final protected def analyze(parser: Parser, topicIn: String, topicOut: String)(implicit
    serdeKIn: Serde[KIN],
    serdeVIn: Serde[VIN],
    serdeKOut: Serde[KOUT],
    serdeVOut: Serde[VOUT]
  ): Unit = {
    val config = readConfig(parser)
    val builder = new StreamsBuilder()
    val stream = builder.stream[KIN, VIN](topicIn)

    val findings = analyzeEvents(stream, config)

    kafkaConfig.sinkStatus match {
      case ACTIVE => findings.to(topicOut)
      case DEBUG  => findings.peek { case (_, finding) => logger.error(s"$finding") }
      case _      => // Do nothing
    }

    val topology = builder.build()
    logger.info(s"${topology.describe}")

    val application = new KafkaStreams(topology, getStreamsConfig(kafkaConfig))

    if (runningLocally) application.cleanUp()

    application.start()

    sys.addShutdownHook {
      application.close()
    }

  }

}
