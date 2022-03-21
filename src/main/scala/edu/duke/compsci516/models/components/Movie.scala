package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import edu.duke.compsci516.models.entity.{APIMovie, MovieDetailRow}
import edu.duke.compsci516.models.database.{MovieDetailTable, GenreTable}
import edu.duke.compsci516.models.entity.MovieDetailRow
import edu.duke.compsci516.models.entity.Genre

trait MovieRepositoryComponent {
  def add(movie: MovieDetailRow): Future[Int]
  // def get(movieId: Int): Future[APIMovie]
//  def getMovie(movieId: Int): Future[Option[Movie]]
}

class MovieRepository(db: Database) extends MovieRepositoryComponent {
  protected val movieDetailTable = MovieDetailTable
  protected val genreTable = GenreTable
  import movieDetailTable.profile.api._
  import movieDetailTable.MovieDetailRows
  import genreTable.GenreRows

  override def add(movie: MovieDetailRow): Future[Int] = db.run {
    MovieDetailRows += movie
  }

  // override def get(movieId: Int): Future[APIMovie] = db.run {}
}
