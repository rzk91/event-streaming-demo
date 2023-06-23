package com.rzk

import com.rzk.runconfig.RunConfiguration.KafkaConfig
import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.scala.serialization.Serdes

import java.util.Properties

package object kstreams extends LazyLogging {

  implicit def serde[A >: Null: Decoder: Encoder]: Serde[A] = {
    val serializer = (a: A) => a.asJson.noSpaces.getBytes
    val deserializer = (aAsBytes: Array[Byte]) => {
      val aAsString = new String(aAsBytes)
      decode[A](aAsString) match {
        case Right(value) => Option(value)
        case Left(error) =>
          logger.debug(s"Error while converting message $aAsString", error)
          Option.empty
      }
    }

    Serdes.fromFn[A](serializer, deserializer)
  }

  def getStreamsConfig(kafkaConfig: KafkaConfig): Properties = {
    val props = new Properties()
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers.mkString(","))
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaConfig.applicationId)
    props.putAll(kafkaConfig.auth.asProperties)
    props.put(StreamsConfig.producerPrefix(ProducerConfig.BATCH_SIZE_CONFIG), kafkaConfig.maxBatch)
    props.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG), kafkaConfig.compressionType)
    props.put(
      StreamsConfig.consumerPrefix(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG),
      Serdes.stringSerde.getClass
    )
    props.put(
      StreamsConfig.producerPrefix(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG),
      Serdes.stringSerde.getClass
    )
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, kafkaConfig.processingGuarantee)

    props
  }
}
