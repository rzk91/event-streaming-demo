package com.rzk.flink

import com.rzk.common.TimestampedObject
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner

class SimpleTimestampAssigner[A <: TimestampedObject] extends SerializableTimestampAssigner[A] {
  override def extractTimestamp(element: A, recordTimestamp: Long): Long = element.timestamp
}
