package edu.duke.imdb.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import java.nio.file.{Path, Paths}

import _root_.edu.duke.imdb.components._
import edu.duke.imdb.components.SparkComponent
import edu.duke.imdb.data.delta.tables.ml.MovieLensSpark
import edu.duke.imdb.data.StorageType

object CollabFilter extends ConfigComponent with SparkComponent {
  this: ConfigComponent =>
  def main(args: Array[String]): Unit = {

    val movieLensLoader =
      new MovieLensSpark(
        databaseName = "ml-25m",
        storageType = StorageType.fs_csv
      )

    val ratings_df = movieLensLoader.ratings_df.get

    val movies_df = movieLensLoader.movies_df.get
      .select(
        col("movieId"),
        col("title"),
        split(col("genres"), "\\|").as("genres")
      )

    val set = ratings_df.randomSplit(Array(0.9, 0.1), seed = 12345)
    val training = set(0).cache()
    val test = set(1).cache()

    println(s"Training: ${training.count()}, test: ${test.count()}")
  }
}
