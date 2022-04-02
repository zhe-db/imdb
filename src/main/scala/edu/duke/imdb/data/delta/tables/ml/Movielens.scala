package edu.duke.imdb.data.delta.tables.ml

import edu.duke.imdb.components.ConfigComponent
import java.nio.file.{Path, Paths}
import edu.duke.imdb.components.SparkComponent
import edu.duke.imdb.data.StorageType
import org.apache.spark.sql.DataFrame

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
  val tagsFile =
    s"${datasetPath}/${this.config.getString(s"movielens.${datasetName}.tags")}"
}

class MovieLensSpark(databaseName: String, storageType: StorageType.Storage)
    extends ConfigComponent
    with SparkComponent {
  var ratings_df: Option[DataFrame] = None;
  var movies_df: Option[DataFrame] = None
  var links_df: Option[DataFrame] = None
  var tags_df: Option[DataFrame] = None

  storageType match {
    case StorageType.fs_csv | StorageType.hdfs_csv => {
      val movieLens = new MovieLensComponent(databaseName, storageType)
      ratings_df = Some(
        spark.read.option("header", "true").csv(movieLens.ratingsFile.toString)
      )
      movies_df = Some(
        spark.read.option("header", "true").csv(movieLens.moviesFile.toString)
      )
      links_df = Some(
        spark.read.option("header", "true").csv(movieLens.linksFile.toString)
      )
      tags_df = Some(
        spark.read.option("header", "true").csv(movieLens.tagsFile.toString)
      )
    }
  }
}

class MovieLensHDFSComponent extends ConfigComponent {}
