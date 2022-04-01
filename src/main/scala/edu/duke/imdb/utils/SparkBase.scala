package edu.duke.imdb.utils

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, SQLContext}
import edu.duke.imdb.components.ConfigComponent

trait SparkComponent extends ConfigComponent {
  val appName = this.config.getString("spark.appName")
  val masterURL = this.config.getString("spark.masterURL")

  val spark = SparkSession
    .builder()
    .appName(appName)
    .master(masterURL)
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
    .config(
      "spark.sql.catalog.spark_catalog",
      "org.apache.spark.sql.delta.catalog.DeltaCatalog"
    )
    .getOrCreate()
}
