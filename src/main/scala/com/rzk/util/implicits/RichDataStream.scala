package com.rzk.util.implicits

import com.ariskk.flink4s.DataStream
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.connector.sink2.Sink
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction

class RichDataStream[A](private val stream: DataStream[A]) extends AnyVal {

  def debug(
    formatter: A => String = (a: A) => a.toString,
    logger: String => Unit = println(_)
  )(implicit typeInfo: TypeInformation[A]): DataStream[A] =
    stream.map { a =>
      logger(formatter(a))
      a
    }

  def toSink(sinkFunction: SinkFunction[A]): DataStreamSink[A] = stream.addSink(sinkFunction)
  def toSink(sink: Sink[A]): DataStreamSink[A] = stream.stream.sinkTo(sink)
}
