package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.database.UserReviewMovieTable
import _root_.edu.duke.imdb.models.entity.UserReview

trait UserReviewMovieRepositoryComponent {
  def add(userReview: UserReview): Future[Int]
  def edit(userReview: UserReview): Future[Int]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def getUserReviews(userId: java.util.UUID): Future[Seq[UserReview]]
  def getReviewsByMovie(movieId: Int): Future[Seq[UserReview]]
}

class UserReviewMovieRepository(db: Database)
    extends UserReviewMovieRepositoryComponent {
  protected val table = UserReviewMovieTable
  import table.profile.api._
  import table.UserReviewMovieRows

  override def add(userReview: UserReview): Future[Int] = db.run {
    UserReviewMovieRows += userReview
  }

  override def edit(userReview: UserReview): Future[Int] = db.run {
    UserReviewMovieRows.filter(_.id === userReview.id).update(userReview)
  }

  override def deleteUser(userId: java.util.UUID): Future[Int] = db.run {
    UserReviewMovieRows.filter(_.userId === userId).delete
  }

  override def deleteMovie(movieId: Int): Future[Int] = db.run {
    UserReviewMovieRows.filter(_.movieId === movieId).delete
  }

  override def getUserReviews(
      userId: java.util.UUID
  ): Future[Seq[UserReview]] =
    db.run {
      UserReviewMovieRows.filter(_.userId === userId).result
    }

  override def getReviewsByMovie(movieId: Int): Future[Seq[UserReview]] =
    db.run {
      UserReviewMovieRows.filter(_.movieId === movieId).result
    }
}
