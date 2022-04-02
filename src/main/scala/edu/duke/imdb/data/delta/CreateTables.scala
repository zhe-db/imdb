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

object DeltaTables extends ConfigComponent {
  val movieDetailTable = new MoviedDetailDeltaTable()
  val movieGenreTable = new MoviedGenreDeltaTable()
  val movieCrewTable = new MovieCrewDeltaTable()
  val userRatingTable = new UserRatingDeltaTable()

  def main(args: Array[String]) {
    // createTable()
    movieCrewTable.readData().toDF().show(5)
    movieGenreTable.readData().toDF().show(5)
    movieDetailTable.readData().toDF().show(5)
  }

  def createTable() {
    movieDetailTable.createTable()
    movieGenreTable.createTable()
    movieCrewTable.createTable()
    userRatingTable.createTable()
  }
}
