package com.rzk.fss

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.rzk.common.{AnalyticsBase, Event, Finding}
import com.rzk.runconfig.RunConfiguration
import com.rzk.util.SinkStatus.{ACTIVE, DEBUG}
import com.rzk.util.implicits.durationToFiniteDuration
import fs2.Stream
import fs2.kafka._

abstract class Fs2AnalyticsBase[F[_]](implicit F: Async[F])
    extends Fs2KafkaBase[F, Unit, Event, Unit, Finding]
    with AnalyticsBase {

  def analyzeEvents(
    eventStream: Stream[F, CommittableValue[F, Event]],
    botConfig: Config
  ): Stream[F, CommittableValue[F, Finding]]

  final protected def runAnalysis(parser: Parser): F[Unit] = {
    val output = for {
      runConfig <- RunConfiguration.completeConfigF[F]
      botConfig <- readConfigF[F](parser)
    } yield TransactionalKafkaProducer
      .stream(producerSettings(runConfig.kafka))
      .flatMap { producer =>
        val findings = analyzeEvents(
          readCommittableRecordsFromTopics(runConfig.kafka, runConfig.kafka.topics.events)
            .evalMapFilter[F, CommittableValue[F, Event]] { r =>
              r.record.value match {
                case Right(value) => F.pure(Some(CommittableValue(r.offset, value)))
                case Left(value) =>
                  logger.warn(s"Parsing failure detected: $value")
                  F.pure(None)
              }
            }
            .debug(formatter = _.debug, logger = logger.trace(_)),
          botConfig
        )

        runConfig.kafka.sinkStatus match {
          case ACTIVE =>
            findings
              .mapAsync(10) { case CommittableValue(offset, finding) =>
                val outputRecord = ProducerRecord(runConfig.kafka.topics.findings, (), finding)
                F.delay(TransactionalProducerRecords.one(CommittableProducerRecords.one(outputRecord, offset)))
              }
              .groupWithin(runConfig.checkpointing.chunkSize, runConfig.checkpointing.interval)
              .unchunks
              .evalMap(producer.produce)
          case DEBUG => findings.evalMap(f => F.delay(logger.error(f.debug)))
          case _     => Stream.empty[F]
        }
      }

    output.flatMap(_.compile.drain)
  }
}
