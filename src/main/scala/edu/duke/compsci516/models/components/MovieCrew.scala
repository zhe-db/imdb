package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.compsci516.models.entity.MovieCrew
import _root_.edu.duke.compsci516.models.database.MovieCrewTable

trait MovieCrewRepositoryComponent {
  def add(movieCrew: MovieCrew): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def deleteCrew(crewId: Int): Future[Int]
  def getMovieCrews(movieId: Int): Future[Seq[Int]]
  def getMoviesByCrew(crewId: Int): Future[Seq[Int]]
}

class MovieCrewRepository(db: Database) extends MovieCrewRepositoryComponent {
  protected val table = MovieCrewTable
  import table.profile.api._
  import table.MovieCrewRows

  override def add(movieCrew: MovieCrew): Future[Int] = db.run {
    MovieCrewRows += movieCrew
  }

  override def deleteMovie(movieId: Int): Future[Int] = db.run {
    MovieCrewRows.filter(_.movieId === movieId).delete
  }

  override def deleteCrew(crewId: Int): Future[Int] = db.run {
    MovieCrewRows.filter(_.crewId === crewId).delete
  }

  override def getMovieCrews(movieId: Int): Future[Seq[Int]] = db.run {
    MovieCrewRows.filter(_.movieId === movieId).map(_.crewId).result
  }

  override def getMoviesByCrew(crewId: Int): Future[Seq[Int]] = db.run {
    MovieCrewRows.filter(_.crewId === crewId).map(_.movieId).result
  }
}
