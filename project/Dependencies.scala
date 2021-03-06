import sbt._

object Version {
  val akka = "2.6.18"
  val slick = "3.3.3"
  val postgres = "42.3.1"
  val akkaHttp = "10.2.7"
  val json4s = "3.5.5"
  val akkaHttpJson4s = "1.38.2"
  val logbackClassic = "1.2.7"
  val scalaTest = "3.2.10"
  val delta = "1.1.0"
  val spark = "3.2.1"
  val redis = "3.42"
}

object Library {
  val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val akkaSparyJson =
    "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
  val akkaDD = "com.typesafe.akka" %% "akka-cluster-typed" % Version.akka
  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick
  val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % Version.slick
  val postgresql = "org.postgresql" % "postgresql" % Version.postgres
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val json4sNative = "org.json4s" %% "json4s-native" % Version.json4s
  val json4sJackson = "org.json4s" %% "json4s-jackson" % Version.json4s
  val akkaHttpJson4s =
    "de.heikoseeberger" %% "akka-http-json4s" % Version.akkaHttpJson4s
  val logbackClassic =
    "ch.qos.logback" % "logback-classic" % Version.logbackClassic
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % Test
  val codec = "commons-codec" % "commons-codec" % "1.15"
  val cors = "ch.megard" %% "akka-http-cors" % "1.1.3"
  val akkaStreamContrib = "com.typesafe.akka" %% "akka-stream-contrib" % "0.10"
  val akkaStreamHDFS =
    "com.lightbend.akka" %% "akka-stream-alpakka-hdfs" % "3.0.4"
  val hive = "io.delta" %% "delta-hive" % "0.3.0"
  val delta = "io.delta" %% "delta-core" % Version.delta
  val deltaStandAlone = "io.delta" %% "delta-standalone" % "0.3.0"
  val scallop = "org.rogach" %% "scallop" % "3.5.1"
  val hadoopClient = "org.apache.hadoop" % "hadoop-client" % "3.1.0"
  val parquet = "org.apache.parquet" % "parquet-hadoop" % "1.10.1"
  val spark = "org.apache.spark" %% "spark-sql" % Version.spark
  val sparkStreaming =
    "org.apache.spark" % "spark-streaming_2.12" % Version.spark % "provided"
  val sparkMLLib = "org.apache.spark" %% "spark-mllib" % Version.spark
}

object Dependencies {

  import Library._

  val depends = Seq(
    akkaActor,
    akkaStream,
    akkaSparyJson,
    slick,
    slickHikariCP,
    slickCodegen,
    postgresql,
    akkaHttp,
    logbackClassic,
    scalaTest,
    codec,
    cors,
    delta,
    hive,
    scallop,
    hadoopClient,
    parquet,
    akkaStreamContrib,
    akkaStreamHDFS,
    spark,
    sparkStreaming,
    sparkMLLib,
    akkaDD
  )

}
