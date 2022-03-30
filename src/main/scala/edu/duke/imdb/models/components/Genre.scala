package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.database.{GenreTable}
import _root_.edu.duke.imdb.models.entity.Genre
import _root_.edu.duke.imdb.models.entity.MovieListItem

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
