package edu.duke.imdb.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import java.nio.file.{Path, Paths}

import _root_.edu.duke.imdb.components._
import edu.duke.imdb.utils.SparkComponent

object CollabFilter extends ConfigComponent with SparkComponent {
  this: ConfigComponent =>
  def main(args: Array[String]): Unit = {
    lazy val input_dir: String = config.getString("data.input_dir")
    lazy val ratings_file: Path =
      Paths.get(input_dir, config.getString("data.ratings"))
    lazy val movies_file: Path =
      Paths.get(input_dir, config.getString("data.movies"))

    import spark.implicits._

    val ratings_df =
      spark.read.option("header", "true").csv(ratings_file.toString)
    ratings_df.show(5)

    val movies_df =
      spark.read
        .option("header", "true")
        .csv(movies_file.toString)
        .select(
          col("movieId"),
          col("title"),
          split(col("genres"), "\\|").as("genres")
        )
    movies_df.show(5)

    val set = ratings_df.randomSplit(Array(0.9, 0.1), seed = 12345)
    val training = set(0).cache()
    val test = set(1).cache()

    println(s"Training: ${training.count()}, test: ${test.count()}")
  }
}
