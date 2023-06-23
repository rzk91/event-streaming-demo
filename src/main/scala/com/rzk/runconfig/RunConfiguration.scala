package com.rzk.runconfig

import cats.effect.Sync
import cats.syntax.eq._
import com.ariskk.flink4s.StreamExecutionEnvironment
import com.rzk.util.ExecutionEnvironment.{Env, LOCAL}
import com.rzk.util.SinkStatus.SinkStatus
import com.rzk.util.implicits.enrichTime
import com.typesafe.scalalogging.LazyLogging
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.streams.StreamsConfig
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

import java.util.Properties
import scala.concurrent.duration.Duration

object RunConfiguration extends LazyLogging {

  logger.debug(s"Effective configuration: ${source.config().map(_.root.render)}")

  // Config objects
  def completeConfigF[F[_]: Sync]: F[Config] = source.loadF[F, Config]()
  lazy val completeConfig: Config = source.loadOrThrow[Config]
  lazy val executionEnvironmentConfig: ExecutionExecutionConfig = completeConfig.executionEnvironment
  lazy val kafkaConfig: KafkaConfig = completeConfig.kafka
  lazy val checkpointingConfig: CheckpointingConfig = completeConfig.checkpointing
  lazy val userConfig: UserConfig = completeConfig.user

  // Helper methods
  lazy val runningLocally: Boolean = executionEnvironmentConfig.environment === LOCAL

  // Case classes for each config category
  case class Config(
    executionEnvironment: ExecutionExecutionConfig,
    kafka: KafkaConfig,
    checkpointing: CheckpointingConfig,
    user: UserConfig
  )

  case class ExecutionExecutionConfig(environment: Env, parallelism: Int)
  case class KafkaTopics(events: String, findings: String)

  case class KafkaAuth(required: Boolean, securityProtocol: String, saslMechanism: String, jaasConfig: String) {

    def asProperties: Properties = {
      val prop = new Properties()
      if (required) {
        prop.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol)
        prop.setProperty(SaslConfigs.SASL_MECHANISM, saslMechanism)
        prop.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig)
      }

      prop
    }
  }

  case class KafkaOffset(enableOffsetCommit: Boolean, startFromEarliest: Boolean)

  case class KafkaConfig(
    bootstrapServers: List[String],
    auth: KafkaAuth,
    groupIdPrefix: String,
    deliveryGuarantee: DeliveryGuarantee,
    topics: KafkaTopics,
    maxBatch: Int,
    offset: KafkaOffset,
    sinkStatus: SinkStatus
  ) {
    val compressionType: String = "zstd"

    def groupId(consumer: Boolean, suffix: String): String =
      s"${kafkaConfig.groupIdPrefix}-${userConfig.name}-kafka-${if (consumer) "reader" else "writer"}-$suffix"

    // Required for Kafka Streams
    def applicationId: String = s"${kafkaConfig.groupIdPrefix}-${userConfig.name}-kafka-kstreams"

    def processingGuarantee: String = deliveryGuarantee match {
      case DeliveryGuarantee.EXACTLY_ONCE  => StreamsConfig.EXACTLY_ONCE_V2
      case DeliveryGuarantee.AT_LEAST_ONCE => StreamsConfig.AT_LEAST_ONCE
      case DeliveryGuarantee.NONE          => StreamsConfig.AT_LEAST_ONCE // Using "AT_LEAST_ONCE" as default
    }

    // Required for FS2/ZIO streams
    def additionalProducerProperties: Properties = {
      val props = new Properties()
      props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
      props.put(ProducerConfig.ACKS_CONFIG, "all")
      props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE)
      props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1)
      props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10.seconds)
      props.put(ProducerConfig.LINGER_MS_CONFIG, 5.seconds)
      props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 17.seconds)
      props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType)
      props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 60.seconds)
      props
    }
  }

  case class CheckpointingConfig(
    interval: Duration,
    chunkSize: Int,
    mode: CheckpointingMode,
    useCompression: Boolean,
    minPauseBetweenCheckpoints: Duration,
    timeout: Duration,
    maxConcurrentCheckpoints: Int
  ) {

    def setFlinkEnvCheckpointing(env: StreamExecutionEnvironment): StreamExecutionEnvironment = {
      val javaEnv = env.javaEnv
      javaEnv.enableCheckpointing(interval.toMillis, mode)
      javaEnv.getConfig.setUseSnapshotCompression(useCompression)
      javaEnv.getCheckpointConfig.setMinPauseBetweenCheckpoints(minPauseBetweenCheckpoints.toMillis)
      javaEnv.getCheckpointConfig.setCheckpointTimeout(timeout.toMillis)
      javaEnv.getCheckpointConfig.setMaxConcurrentCheckpoints(maxConcurrentCheckpoints)

      if (runningLocally) {
        javaEnv.setStateBackend(new HashMapStateBackend())
      } else {
        javaEnv.getCheckpointConfig.setExternalizedCheckpointCleanup(
          ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION
        )
      }

      StreamExecutionEnvironment(javaEnv)
    }
  }

  case class UserConfig(name: String)
}
