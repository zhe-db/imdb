package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.compsci516.models.database.UserFavouriteMovieTable
import _root_.edu.duke.compsci516.models.entity.UserFavouriteMovie

trait UserFavouriteMovieRepositoryComponent {
  def add(userFavMovie: UserFavouriteMovie): Future[Int]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def getUserFavouriteMovies(userId: java.util.UUID): Future[Seq[Option[Int]]]
  def getUsersByMovie(movieId: Int): Future[Seq[java.util.UUID]]
}

class UserFavouriteMovieRepository(db: Database)
    extends UserFavouriteMovieRepositoryComponent {
  protected val table = UserFavouriteMovieTable
  import table.profile.api._
  import table.UserFavouriteMovieRows

  override def add(userFavMovie: UserFavouriteMovie): Future[Int] = db.run {
    UserFavouriteMovieRows += userFavMovie
  }

  override def deleteUser(userId: java.util.UUID): Future[Int] = db.run {
    UserFavouriteMovieRows.filter(_.userId === userId).delete
  }

  override def deleteMovie(movieId: Int): Future[Int] = db.run {
    UserFavouriteMovieRows.filter(_.movieId === movieId).delete
  }

  override def getUserFavouriteMovies(
      userId: java.util.UUID
  ): Future[Seq[Option[Int]]] =
    db.run {
      UserFavouriteMovieRows.filter(_.userId === userId).map(_.movieId).result
    }

  override def getUsersByMovie(movieId: Int): Future[Seq[java.util.UUID]] =
    db.run {
      UserFavouriteMovieRows.filter(_.movieId === movieId).map(_.userId).result
    }
}
