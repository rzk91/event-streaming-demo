package com.rzk

import cats.effect._
import fs2.kafka._
import io.circe._
import io.circe.parser._
import io.circe.syntax._

package object fss {

  implicit def fs2Deserializer[F[_], A: Decoder](implicit F: Sync[F]): Deserializer[F, Either[Throwable, A]] =
    Deserializer
      .lift(bytes => F.fromEither(decode[A](new String(bytes))))
      .option
      .attempt
      .map(_.flatMap(_.toRight(new RuntimeException("Deserialization error"))))

  implicit def fs2Serializer[F[_], A: Encoder](implicit F: Sync[F]): Serializer[F, A] =
    Serializer.lift(j => F.pure(j.asJson.noSpaces.getBytes))
}
