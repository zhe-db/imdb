package edu.duke.imdb.components

import org.apache.spark.sql.{SparkSession, SQLContext}
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._ // not necessary since Spark 1.3

import _root_.edu.duke.imdb.components.ConfigComponent

trait SparkComponent extends ConfigComponent {
  val appName = this.config.getString("spark.appName")
  val masterURL = this.config.getString("spark.masterURL")

  implicit val spark = SparkSession
    .builder()
    .appName(appName)
    .master(masterURL)
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config(
      "spark.delta.logStore.class",
      "org.apache.spark.sql.delta.storage.HDFSLogStore"
    )
    .config(
      "spark.sql.catalog.spark_catalog",
      "org.apache.spark.sql.delta.catalog.DeltaCatalog"
    )
    .getOrCreate()
  val sc = spark.sparkContext
  sc.setLogLevel("OFF")
}
