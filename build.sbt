ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name             := "event-streaming-demo",
    libraryDependencies ++=
      flinkDependencies ++
      kafkaStreamDependencies ++
      fs2Dependencies ++
      circeDependencies ++
      otherDependencies
  )

val flinkVersion = "1.15.2"
val kafkaVersion = "3.4.0"
val fs2Version = "3.6.1"
val fs2KafkaVersion = "3.0.0-M8"
val circeVersion = "0.14.1"

val flinkDependencies = Seq(
  "com.ariskk"      %% "flink4s"               % flinkVersion, // Flink with Scala wrapper
  "org.apache.flink" % "flink-connector-kafka" % flinkVersion,
  "org.apache.flink" % "flink-clients"         % flinkVersion
)

val kafkaStreamDependencies = Seq(
  "org.apache.kafka"  % "kafka-clients"       % kafkaVersion,
  "org.apache.kafka"  % "kafka-streams"       % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion
)

val fs2Dependencies = Seq(
  "co.fs2"          %% "fs2-core"  % fs2Version,
  "co.fs2"          %% "fs2-io"    % fs2Version,
  "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion
)

val circeDependencies = Seq(
  "io.circe" %% "circe-core"           % circeVersion,
  "io.circe" %% "circe-generic"        % circeVersion,
  "io.circe" %% "circe-parser"         % circeVersion,
  "io.circe" %% "circe-optics"         % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"             % "0.17.4",
  "com.github.pureconfig"      %% "pureconfig-cats-effect" % "0.17.4",
  "org.slf4j"                   % "slf4j-log4j12"          % "2.0.5",
  "com.typesafe.scala-logging" %% "scala-logging"          % "3.9.5",
  // Mainly for mac users
  "commons-io"        % "commons-io"  % "2.11.0",
  "org.xerial.snappy" % "snappy-java" % "1.1.9.1"
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Ywarn-unused:imports",
  "-Xfatal-warnings",
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
