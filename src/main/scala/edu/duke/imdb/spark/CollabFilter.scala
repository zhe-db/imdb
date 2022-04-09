package edu.duke.imdb.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark._

import java.nio.file.{Path, Paths}

import _root_.edu.duke.imdb.components._
import edu.duke.imdb.components.SparkComponent
import edu.duke.imdb.data.delta.tables.ml.MovieLensSpark
import edu.duke.imdb.data.StorageType

object CollabFilterTrainer extends ConfigComponent with SparkComponent {
  this: ConfigComponent =>
  import spark.implicits._
  val trainingSetPercentage =
    config.getDouble("collab_filter.training.training_set_percentage")
  val rank = config.getInt("collab_filter.training.rank")
  val numIterations = config.getInt("collab_filter.training.numIterations")
  val modelPath = config.getString("collab_filter.model.save_path")

  val movieLensLoader =
    new MovieLensSpark(
      databaseName = "ml-1m",
      storageType = StorageType.fs_csv
    )

  val ratings_df = movieLensLoader.ratings_df.get
  val movies_df = movieLensLoader.movies_df.get
  val ratingsMapped_df = movieLensLoader.ratingsMapped_df.get
  val normalizedRatings = ratingsMapped_df
    .select(
      $"userId".as("user"),
      $"ratings".as("rating"),
      $"movieId".as("product")
    )
  val set = normalizedRatings.randomSplit(
    Array(trainingSetPercentage, 1 - trainingSetPercentage),
    seed = 12345
  )
  val training = set(0).cache()
  val test = set(1).cache()
  val trainingRDD = training.as[Rating].rdd

  def main(args: Array[String]): Unit = {
    ratingsMapped_df.show(100)
  }

  def trainModel(rank: Int, numIterations: Int) {
    val model: MatrixFactorizationModel =
      ALS.train(trainingRDD, rank, numIterations, 0.01)
    saveModel(model)
  }

  def saveModel(model: MatrixFactorizationModel): Unit = {
    try {
      model.save(spark.sparkContext, modelPath)
    } catch {
      case e: Exception => println(s"Failed to save model: ${e.toString()}")
    } finally {}
  }
}

object CollabFilter extends ConfigComponent with SparkComponent {
  val modelPath = config.getString("collab_filter.model.save_path")
  val sc = spark.sparkContext
  val model: MatrixFactorizationModel =
    MatrixFactorizationModel.load(sc, modelPath)

}
