package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.compsci516.models.database.UserViewMovieTable
import _root_.edu.duke.compsci516.models.entity.UserView

trait UserViewMovieRepositoryComponent {
  def add(userView: UserView): Future[Int]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def getUserViews(userId: java.util.UUID): Future[Seq[UserView]]
  def getViewsByMovie(movieId: Int): Future[Seq[UserView]]
}

class UserViewMovieRepository(db: Database)
    extends UserViewMovieRepositoryComponent {
  protected val table = UserViewMovieTable
  import table.profile.api._
  import table.UserViewMovieRows

  override def add(userView: UserView): Future[Int] = db.run {
    UserViewMovieRows += userView
  }

  override def deleteUser(userId: java.util.UUID): Future[Int] = db.run {
    UserViewMovieRows.filter(_.userId === userId).delete
  }

  override def deleteMovie(movieId: Int): Future[Int] = db.run {
    UserViewMovieRows.filter(_.movieId === movieId).delete
  }

  override def getUserViews(
      userId: java.util.UUID
  ): Future[Seq[UserView]] =
    db.run {
      UserViewMovieRows.filter(_.userId === userId).result
    }

  override def getViewsByMovie(movieId: Int): Future[Seq[UserView]] =
    db.run {
      UserViewMovieRows.filter(_.movieId === movieId).result
    }
}
