package edu.duke.imdb.data.delta

import edu.duke.imdb.data.delta.tables.MovieDetailDeltaTableComp
import _root_.edu.duke.imdb.components._
import _root_.edu.duke.imdb.models.components._
import scala.util.Success
import scala.util.Failure
import breeze.linalg.randomDouble

object MovieDetailTableBenchmarks
    extends SparkComponent
    with DatabaseComponent {

  val movieDetailTable = new MovieRepository(this.db)
  val movieDetailDeltaTable = new MovieDetailDeltaTableComp()
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  def benchmarkMovieDetailTable() {
    val ids = movieDetailDeltaTable.getMovieIds(1000)
    // benchmarkRandomSelectById(ids)
    // benchmarkRandomSelectByRatings(1000, 1000)
    benchmarkRandomSort(125, 100)
    // benchamrRandomBatchRead(1000, 1000)
  }

  def benchmarkRandomSelectById(ids: Seq[String]): Unit = {
    val t0 = System.nanoTime()
    for (id <- ids) {
      val result = movieDetailDeltaTable.get(id)
    }
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
  }

  def benchmarkRandomSelectByRatings(total: Int, limit: Int): Unit = {
    val t0 = System.nanoTime()
    for (id <- 1 to total) {
      val result =
        movieDetailDeltaTable.getMoviesByRatings(randomDouble() * 10, limit)
    }
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
  }

  def benchamrRandomBatchRead(total: Int, limit: Int): Unit = {
    val t0 = System.nanoTime()
    for (id <- 1 to total) {
      val result =
        movieDetailDeltaTable.batchRead(limit)
    }
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
  }

  def benchmarkRandomSort(total: Int, limit: Int): Unit = {
    val t0 = System.nanoTime()
    for (id <- 1 to total) {
      for (
        col <- Array(
          "id",
          "vote_average",
          "vote_count",
          "budget",
          "revenue",
          "title",
          "poster_path",
          "popularity"
        )
      ) {
        val result =
          movieDetailDeltaTable.getPaginatedMovies(col, limit)
      }
    }
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
  }

  def main(args: Array[String]): Unit = {
    benchmarkMovieDetailTable()
  }
}
