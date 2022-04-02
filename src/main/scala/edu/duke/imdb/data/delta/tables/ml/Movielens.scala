package edu.duke.imdb.data.delta.tables.ml

import edu.duke.imdb.components.ConfigComponent
import java.nio.file.{Path, Paths}
import edu.duke.imdb.components.SparkComponent
import edu.duke.imdb.data.StorageType
import org.apache.spark.sql.DataFrame

trait MovieLens extends ConfigComponent {}

class MovieLensComponent(datasetName: String) extends MovieLens {
  val datasetPath = this.config.getString(s"movielens.${datasetName}.input_dir")
  val ratingsFile: Path = Paths.get(
    datasetPath,
    this.config.getString(s"movielens.${datasetName}.ratings")
  )
  val moviesFile: Path =
    Paths.get(datasetPath, config.getString(s"movielens.${datasetName}.movies"))
  val linksFile: Path =
    Paths.get(datasetPath, config.getString(s"movielens.${datasetName}.links"))
  val tagsFile: Path =
    Paths.get(datasetPath, config.getString(s"movielens.${datasetName}.tags"))
}

class MovieLensSpark(databaseName: String, storageType: StorageType.Storage)
    extends ConfigComponent
    with SparkComponent {
  var ratings_df: Option[DataFrame] = None;
  var movies_df: Option[DataFrame] = None
  var links_df: Option[DataFrame] = None
  var tags_df: Option[DataFrame] = None

  storageType match {
    case StorageType.fs_csv => {
      val movieLens = new MovieLensComponent(databaseName)
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
