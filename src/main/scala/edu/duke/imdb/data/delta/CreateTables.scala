package edu.duke.imdb.data.delta

import scala.collection.JavaConverters._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, SQLContext}
import io.delta.tables._

import org.apache.spark.sql.functions._

import _root_.edu.duke.imdb.components._

object DeltaTables extends ConfigComponent {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder()
      .appName("Quickstart")
      .master("local[*]")
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config(
        "spark.sql.catalog.spark_catalog",
        "org.apache.spark.sql.delta.catalog.DeltaCatalog"
      )
      .getOrCreate()

    val save_path = this.config.getString("delta.save_path")

    spark.sql(s"""
     CREATE TABLE IF NOT EXISTS default.moviedetail (
     id STRING,
     adult BOOLEAN,
     backdrop_path STRING,
     budget INT,
     imdb_id STRING,
     title STRING,
     overview STRING,
     popularity DOUBLE,
     poster_path STRING,
     runtime INT,
     revenue INT,
     vote_average DOUBLE,
     homepage STRING,
     vote_count INT,
     tagline STRING
     ) USING DELTA LOCATION '${save_path}/moviedetail' 
     """)

    spark.sql(s"""
     CREATE TABLE IF NOT EXISTS default.userratings (
     id STRING,
     user_id STRING,
     movie_id INT,
     rating DOUBLE
     ) USING DELTA LOCATION '${save_path}/userratings' 
     """)

    spark.sql(s"""
     CREATE TABLE IF NOT EXISTS default.moviegenre (
     movie_id INT,
     genre_id INTEGER
     ) USING DELTA LOCATION '${save_path}/moviegenre' 
    """)

    spark.sql(s"""
     CREATE TABLE IF NOT EXISTS default.moviecrew (
     movie_id INT,
     crew_id INT,
     types STRING,
     cast_id INTEGER,
     character STRING,
     job STRING,
     department STRING,
     credit_id STRING,
     ordering INTEGER
     ) USING DELTA LOCATION '${save_path}/moviecrew' 
     """)
    val connector = DeltaConnector
    connector.main(Array.empty[String])
  }
}
