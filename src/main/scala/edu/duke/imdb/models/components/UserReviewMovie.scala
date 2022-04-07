package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.database.UserReviewMovieTable
import _root_.edu.duke.imdb.models.entity._
import java.util.UUID

trait UserReviewMovieRepositoryComponent {
  def add(userReview: UserReview): Future[Int]
  def edit(userReview: APIUserReview): Future[Int]
  def edit(userReview: UserReview): Future[Int]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def deleteReview(reviewId: UUID): Future[Int]
  def getUserReviews(userId: java.util.UUID): Future[Seq[UserReview]]
  def getReviewsByMovie(movieId: Int): Future[Seq[UserReview]]
  def getMovieReview(reviewId: java.util.UUID): Future[Option[UserReview]]
}

class UserReviewMovieRepository(db: Database)
    extends UserReviewMovieRepositoryComponent {
  protected val table = UserReviewMovieTable
  import table.profile.api._
  import table.UserReviewMovieRows

  override def add(userReview: UserReview): Future[Int] = db.run {
    UserReviewMovieRows += userReview
  }

  override def edit(userReview: APIUserReview): Future[Int] = db.run {
    UserReviewMovieRows
      .filter(r =>
        r.movieId === userReview.movieId && r.userId === userReview.userId
      )
      .map(_.contents)
      .update(Some(userReview.contents))
  }

  override def edit(userReview: UserReview): Future[Int] = db.run {
    UserReviewMovieRows
      .filter(r => r.id === userReview.id)
      .update(userReview)
  }

  override def deleteReview(reviewId: UUID): Future[Int] = db.run {
    UserReviewMovieRows.filter(_.id === reviewId).delete
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

  override def getMovieReview(reviewId: UUID): Future[Option[UserReview]] =
    db.run {
      UserReviewMovieRows.filter(_.id === reviewId).result.headOption
    }
}
