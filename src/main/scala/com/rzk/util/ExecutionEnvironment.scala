package com.rzk.util

import cats.kernel.Eq

import scala.util.Try

object ExecutionEnvironment extends Enumeration {
  type Env = Value
  val LOCAL, STAGE, PROD = Value

  implicit class ExecEnvOps(private val env: String) extends AnyVal {
    def toEnv: Option[Env] = Try(withName(env.toUpperCase)).toOption
  }

  implicit val execEnvEq: Eq[Env] = Eq.instance((env1, env2) => env1 == env2)
}
