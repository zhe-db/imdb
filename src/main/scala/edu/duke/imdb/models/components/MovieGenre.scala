package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import edu.duke.imdb.models.entity.MovieGenre
import edu.duke.imdb.models.database.MovieGenreTable

trait MovieGenreRepositoryComponent {
  def add(movieGenre: MovieGenre): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def deleteGenre(genreId: Int): Future[Int]
  def getMovieGenres(movie: Int): Future[Seq[Int]]
  def getMoviesByGenre(genreId: Int): Future[Seq[Int]]
}

class MovieGenreRepository(db: Database) extends MovieGenreRepositoryComponent {
  protected val table = MovieGenreTable
  import table.profile.api._
  import table.MovieGenreRows

  override def add(movieGenre: MovieGenre): Future[Int] = db.run {
    MovieGenreRows += movieGenre
  }

  override def deleteMovie(movieId: Int): Future[Int] = db.run {
    MovieGenreRows.filter(_.movieId === movieId).delete
  }

  override def deleteGenre(genreId: Int): Future[Int] = db.run {
    MovieGenreRows.filter(_.genreId === genreId).delete
  }

  override def getMovieGenres(movieId: Int): Future[Seq[Int]] = db.run {
    MovieGenreRows.filter(_.movieId === movieId).map(_.genreId).result
  }

  override def getMoviesByGenre(genreId: Int): Future[Seq[Int]] = db.run {
    MovieGenreRows.filter(_.genreId === genreId).map(_.movieId).result
  }
}
