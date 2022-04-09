package edu.duke.imdb.data.delta.tables

import _root_.edu.duke.imdb.components.SparkComponent
import _root_.edu.duke.imdb.components.ConfigComponent
import _root_.edu.duke.imdb.data.delta.tables.ml.MovieLensComponent
import _root_.edu.duke.imdb.data.StorageType
import _root_.edu.duke.imdb.data.delta.tables.ml.MovieLensSpark
import org.apache.spark.sql.DataFrame

class MovieLensDeltaTable(tableName: String, tableSchema: String)
    extends SparkComponent
    with ConfigComponent {
  var savePath: String = this.config.getString("delta.save_path")
  var tablePath: String = s"${this.savePath}/${tableName}"
  val createTableSql = s"""
     CREATE TABLE IF NOT EXISTS default.${this.tableName} (
       ${this.tableSchema}
     ) USING DELTA LOCATION '${savePath}/${tableName}' 
   """

  def createTable() {
    val movieLensComp = new MovieLensSpark(
      databaseName = "ml-1m",
      storageType = StorageType.fs_csv
    )
    val df = tableName match {
      case "links"   => movieLensComp.links_df
      case "users"   => movieLensComp.users_df
      case "ratings" => movieLensComp.ratings_df
      case "movies"  => movieLensComp.movies_df
      case _         => movieLensComp.movies_df
    }
    df.get.write.format("delta").mode("overwrite").save(tablePath)
  }

  def readData(): DataFrame = {
    return spark.read.format("delta").load(tablePath)
  }
}

class MovieLensLinksDeltaTable
    extends MovieLensDeltaTable(tableName = "links", tableSchema = "")

class MovieLensMoviesDeltaTable
    extends MovieLensDeltaTable(tableName = "movies", tableSchema = "")

class MovieLensRatingsDeltaTable
    extends MovieLensDeltaTable(tableName = "ratings", tableSchema = "")

class MovieLensUsersDeltaTable
    extends MovieLensDeltaTable(tableName = "users", tableSchema = "")

class MovieLensRatingsMappedDeltaTable
    extends MovieLensDeltaTable(tableName = "ratingsMapped", tableSchema = "")
