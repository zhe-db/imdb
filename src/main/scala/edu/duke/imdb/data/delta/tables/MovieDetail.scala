package edu.duke.imdb.data.delta.tables
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import org.apache.spark.sql._
import org.apache.spark._
import org.apache.spark.sql.functions.rand
import _root_.edu.duke.imdb.models.delta._
import _root_.edu.duke.imdb.models.entity.MovieDetailRow

class MoviedDetailDeltaTable
    extends DeltaTableBase(
      tableName = "moviedetail",
      sourceTableName = "moviedetail",
      primaryColumnName = "id",
      tableSchema = s"""
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
      """
    ) {}

class MovieDetailDeltaTableComp extends MoviedDetailDeltaTable {
  import spark.implicits._
  spark.sparkContext.setLogLevel("ERROR")
  val df = this.readData()
  def add(movie: MovieDetailRow): Unit = {
    val newRow = Seq(movie).toDF()
    newRow.write.format("delta").mode("append").save(this.tablePath)
  }

  def get(movieId: String): Array[Row] = {
    df.filter(df("id") === movieId).collect()
  }

  def batchRead(limit: Integer): Array[Row] = {
    df.limit(limit).collect()
  }

  def getMoviesByRatings(rating: Double, limit: Integer): Array[Row] = {
    df.filter(df("vote_average").cast("Double") > rating)
      .limit(limit)
      .collect()
  }

  def getMoviesByRatingsCount(count: Int, limit: Integer): Array[Row] = {
    df.filter(df("vote_count") > count).limit(limit).collect()
  }

  def getPaginatedMovies(
      sortKey: String,
      limit: Int
  ): Array[Row] = {
    df.sort(df.col(sortKey).asc).limit(limit).collect()
  }

  def getMovieIds(limit: Int): Array[String] = {
    df.select("id")
      .orderBy(rand())
      .map(r => r.getString(0))
      .limit(limit)
      .collect()
  }
}
