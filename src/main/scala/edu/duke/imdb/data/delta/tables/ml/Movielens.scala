package edu.duke.imdb.data.delta.tables.ml

import edu.duke.imdb.components.ConfigComponent
import java.nio.file.{Path, Paths}
import edu.duke.imdb.components.SparkComponent
import edu.duke.imdb.data.StorageType
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

import edu.duke.imdb.data.delta.tables.MovieLensRatingsDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensMoviesDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensLinksDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensUsersDeltaTable

trait MovieLens extends ConfigComponent {}

class MovieLensComponent(datasetName: String, storageType: StorageType.Storage)
    extends MovieLens {
  var datasetPath = ""
  storageType match {
    case StorageType.fs_csv | StorageType.fs_delta =>
      datasetPath = this.config.getString(s"movielens.${datasetName}.input_dir")
    case StorageType.hdfs_csv | StorageType.hdfs_delta =>
      datasetPath =
        s"hdfs://${this.config.getString("hdfs.domain")}:${this.config
          .getString("hdfs.port")}/${this.config
          .getString(s"movielens.${datasetName}.hdfs.input_dir")}"
  }

  val ratingsFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${datasetName}.ratings")}"
  val moviesFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${datasetName}.movies")}"
  val linksFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${datasetName}.links")}"
  val usersFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${datasetName}.users")}"
  val usersMappingsFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${datasetName}.usersMapping")}"
}

class MovieLensSpark(databaseName: String, storageType: StorageType.Storage)
    extends ConfigComponent
    with SparkComponent {
  var ratings_df: Option[DataFrame] = None
  var movies_df: Option[DataFrame] = None
  var links_df: Option[DataFrame] = None
  var users_df: Option[DataFrame] = None
  var usersMappings_df: Option[DataFrame] = None
  import spark.sqlContext.implicits._

  storageType match {
    case StorageType.fs_csv | StorageType.hdfs_csv => {
      val movieLens = new MovieLensComponent(databaseName, storageType)
      ratings_df = Some(
        spark.sqlContext.read
          .text(movieLens.ratingsFile.toString)
          .select(split($"value", "::").as("value"))
          .select(
            $"value".getItem(0).cast(IntegerType).as("user"),
            $"value".getItem(1).cast(IntegerType).as("movieId"),
            $"value".getItem(2).cast(FloatType).as("rating")
          )
      )
      movies_df = Some(
        spark.sqlContext.read
          .text(movieLens.moviesFile.toString)
          .select(split($"value", "::").as("value"))
          .select(
            $"value".getItem(0).cast(IntegerType).as("movie"),
            $"value".getItem(1).as("title"),
            split($"value".getItem(2), "\\|").as("genre")
          )
      )
      links_df = Some(
        spark.read.option("header", "true").csv(movieLens.linksFile.toString)
      )
      usersMappings_df = Some(
        spark.read
          .option("header", "true")
          .csv(movieLens.usersMappingsFile.toString)
      )
      users_df = Some(
        spark.sqlContext.read
          .text(movieLens.ratingsFile.toString)
          .select(split($"value", "::").as("value"))
          .select(
            $"value".getItem(0).cast(IntegerType).as("userId"),
            $"value".getItem(1).cast(StringType).as("gender"),
            $"value".getItem(2).cast(IntegerType).as("age"),
            $"value".getItem(3).cast(IntegerType).as("occupation"),
            $"value".getItem(3).cast(StringType).as("zipCode")
          )
      )
    }
    case StorageType.fs_delta | StorageType.hdfs_delta => {
      ratings_df = Some(new MovieLensRatingsDeltaTable().readData().toDF())
      movies_df = Some(new MovieLensMoviesDeltaTable().readData().toDF())
      links_df = Some(new MovieLensLinksDeltaTable().readData().toDF())
      users_df = Some(new MovieLensUsersDeltaTable().readData().toDF())
    }
  }
}

class MovieLensHDFSComponent extends ConfigComponent {}
