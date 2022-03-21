package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.compsci516.models.entity.{APIMovie, MovieDetailRow}
import _root_.edu.duke.compsci516.models.database.{MovieDetailTable, GenreTable}
import _root_.edu.duke.compsci516.models.entity.MovieDetailRow
import _root_.edu.duke.compsci516.models.entity.Genre
import _root_.edu.duke.compsci516.models.entity.MovieDetailRow

trait MovieRepositoryComponent {
  def add(movie: MovieDetailRow): Future[MovieDetailRow]
  // def get(movieId: Int): Future[APIMovie]
//  def getMovie(movieId: Int): Future[Option[Movie]]
}

class MovieRepository(db: Database) extends MovieRepositoryComponent {
  protected val movieDetailTable = MovieDetailTable
  protected val genreTable = GenreTable
  import movieDetailTable.profile.api._
  import movieDetailTable.MovieDetailRows
  import genreTable.GenreRows

  override def add(movie: MovieDetailRow): Future[MovieDetailRow] = db.run {
    (MovieDetailRows returning MovieDetailRows) += movie
  }

  // override def get(movieId: Int): Future[APIMovie] = db.run {}
}
