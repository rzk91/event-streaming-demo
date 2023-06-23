package com.rzk.flink

import cats.kernel.Monoid
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.circe.parser._
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation

class JsonDeserializer[A: Decoder: TypeInformation](implicit M: Monoid[A])
    extends DeserializationSchema[A]
    with LazyLogging {

  override def deserialize(message: Array[Byte]): A = {
    val input = new String(message)
    decode[A](input) match {
      case Left(exception) =>
        logger.error(s"Error parsing $input: ${exception.getMessage}; Returning empty value")
        M.empty
      case Right(value) => value
    }
  }

  override def isEndOfStream(nextElement: A): Boolean = false

  override def getProducedType: TypeInformation[A] = implicitly[TypeInformation[A]]
}
