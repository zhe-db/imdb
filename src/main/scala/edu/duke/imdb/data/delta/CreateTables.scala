package edu.duke.imdb.data.delta

import scala.collection.JavaConverters._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, SQLContext}
import io.delta.tables._

import org.apache.spark.sql.functions._

import _root_.edu.duke.imdb.components._
import edu.duke.imdb.data.delta.tables.MoviedDetailDeltaTable
import edu.duke.imdb.data.delta.tables.edu.duke.imdb.data.delta.tables.MoviedGenreDeltaTable
import edu.duke.imdb.data.delta.tables.MovieCrewDeltaTable
import edu.duke.imdb.data.delta.tables.UserRatingDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensLinksDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensMoviesDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensRatingsDeltaTable
import edu.duke.imdb.data.delta.tables.MovieLensTagsDeltaTable

object DeltaTables extends ConfigComponent {
  val movieDetailTable = new MoviedDetailDeltaTable()
  val movieGenreTable = new MoviedGenreDeltaTable()
  val movieCrewTable = new MovieCrewDeltaTable()
  val userRatingTable = new UserRatingDeltaTable()

  val linksTable = new MovieLensLinksDeltaTable()
  val moviesTable = new MovieLensMoviesDeltaTable()
  val ratingsTable = new MovieLensRatingsDeltaTable()
  val tagsTable = new MovieLensTagsDeltaTable()

  def main(args: Array[String]) {
    linksTable.createTable()
    moviesTable.createTable()
    ratingsTable.createTable()
    tagsTable.createTable()
    // createTable()
    movieCrewTable.readData().toDF().show(5)
    movieCrewTable.readData().toDF().show(5)
    movieGenreTable.readData().toDF().show(5)
    linksTable.readData().toDF().show(5)
    moviesTable.readData().toDF().show(5)
    ratingsTable.readData().toDF().show(5)
    tagsTable.readData().toDF().show(5)
  }

  def createTable() {
    movieDetailTable.createTable()
    movieGenreTable.createTable()
    movieCrewTable.createTable()
    userRatingTable.createTable()
  }
}
