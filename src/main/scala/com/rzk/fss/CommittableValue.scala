package com.rzk.fss

import fs2.kafka.CommittableOffset

case class CommittableValue[F[_], A](offset: CommittableOffset[F], value: A) {
  def debug: String = value.toString
}
