package com.rzk.fss

import cats.effect.Async
import com.rzk.runconfig.RunConfiguration.KafkaConfig
import com.rzk.util.implicits.{enrichProperties, enrichTime}
import fs2.Stream
import fs2.kafka._
import io.circe._

import java.time.ZoneId

abstract class Fs2KafkaBase[F[_], KIN, VIN, KOUT, VOUT](implicit
  F: Async[F],
  kInDecoder: Decoder[KIN],
  vInDecoder: Decoder[VIN],
  kOutEncoder: Encoder[KOUT],
  vOutEncoder: Encoder[VOUT]
) {

  def consumerSettings(kafkaConfig: KafkaConfig): ConsumerSettings[F, Either[Throwable, KIN], Either[Throwable, VIN]] =
    ConsumerSettings[F, Either[Throwable, KIN], Either[Throwable, VIN]]
      .withIsolationLevel(IsolationLevel.ReadCommitted)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(kafkaConfig.bootstrapServers.mkString(","))
      .withProperties(kafkaConfig.auth.asProperties.toMap)
      .withGroupId(kafkaConfig.groupId(consumer = true, "fs2"))
      .withMaxPollRecords(kafkaConfig.maxBatch)

  def producerSettings(kafkaConfig: KafkaConfig): TransactionalProducerSettings[F, KOUT, VOUT] =
    TransactionalProducerSettings(
      kafkaConfig.groupId(consumer = false, "fs2"),
      ProducerSettings[F, KOUT, VOUT]
        .withBootstrapServers(kafkaConfig.bootstrapServers.mkString(","))
        .withProperties(kafkaConfig.auth.asProperties.toMap)
        .withProperties(kafkaConfig.additionalProducerProperties.toMap)
    )

  def logValueWithTimestamp(record: ConsumerRecord[KIN, VIN]): F[Unit] =
    F.delay(println(s"[${record.timestamp.createTime.map(_.asString())}] ${record.value}"))

  def logValueWithTimestamp(value: VIN)(timestamp: VIN => Option[Long], zoneId: VIN => Option[ZoneId]): F[Unit] =
    F.delay(println(s"[${timestamp(value).map(_.asString(zoneId(value)))}] $value"))

  def logValue(value: VIN): F[Unit] = F.delay(println(value))

  def readCommittableRecordsFromTopics(
    kafkaConfig: KafkaConfig,
    topic1: String,
    others: String*
  ): Stream[F, CommittableConsumerRecord[F, Either[Throwable, KIN], Either[Throwable, VIN]]] =
    KafkaConsumer.stream(consumerSettings(kafkaConfig)).subscribeTo(topic1, others: _*).records

  def readRecordsFromTopics(
    kafkaConfig: KafkaConfig,
    topic1: String,
    others: String*
  ): Stream[F, ConsumerRecord[Either[Throwable, KIN], Either[Throwable, VIN]]] =
    readCommittableRecordsFromTopics(kafkaConfig, topic1, others: _*).map(_.record)

  def readValueFromTopics(
    kafkaConfig: KafkaConfig,
    topic1: String,
    others: String*
  ): Stream[F, Either[Throwable, VIN]] =
    readCommittableRecordsFromTopics(kafkaConfig, topic1, others: _*).map(_.record.value)
}
