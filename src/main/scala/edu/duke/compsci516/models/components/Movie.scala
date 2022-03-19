package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import edu.duke.compsci516.models.entity.{Movie, MovieDetailRow}
import edu.duke.compsci516.models.database.{MovieDetailTable, GenreTable}
import edu.duke.compsci516.models.entity.MovieDetailRow
import edu.duke.compsci516.models.entity.Genre

trait MovieRepositoryComponent {
//  def add(movie: Movie): Future[Int]
//  def getMovie(movieId: Int): Future[Option[Movie]]
}

class MovieRepository(db: Database) extends MovieRepositoryComponent {
  protected val movieDetailTable = MovieDetailTable
  import movieDetailTable.profile.api._
  import movieDetailTable.MovieDetailRows
}

trait GenreRepositoryComponent {
  def add(genre: Genre): Future[Int]
  def getGenre(genreId: Int): Future[Option[Genre]]
  def getGenres(): Future[Seq[Genre]]
  def delete(genreId: Int): Future[Int]
  def delete(genre: Genre): Future[Int]
  def update(genre: Genre): Future[Int]
}

class GenreRepository(db: Database) extends GenreRepositoryComponent {
  protected val genreTable = GenreTable
  import genreTable.profile.api._
  import genreTable.GenreRows

  override def add(genre: Genre): Future[Int] = db.run {
    GenreRows += genre
  }

  override def delete(genreId: Int): Future[Int] = db.run {
    GenreRows.filter(_.id === genreId).delete
  }

  override def delete(genre: Genre): Future[Int] = db.run {
    GenreRows.filter(_.id === genre.id).delete
  }

  override def update(genre: Genre): Future[Int] = db.run {
    GenreRows.filter(_.id === genre.id).update(genre)
  }

  override def getGenre(genreId: Int): Future[Option[Genre]] = db.run {
    GenreRows.filter(_.id === genreId).result.headOption
  }

  override def getGenres(): Future[Seq[Genre]] = db.run {
    GenreRows.result
  }
}
