package com.rzk.flink

import io.circe._
import io.circe.syntax._
import org.apache.flink.api.common.serialization.SerializationSchema

class JsonSerializer[A: Encoder] extends SerializationSchema[A] {
  override def serialize(element: A): Array[Byte] = element.asJson.noSpaces.getBytes
}
