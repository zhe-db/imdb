package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.database.UserRateMovieTable
import _root_.edu.duke.imdb.models.entity._
import java.util.UUID

trait UserRateMovieRepositoryComponent {
  def add(userRating: UserRating): Future[UserRating]
  def deleteRating(userMovie: UserMovie): Future[Int]
  def editRating(userRating: APIUserRating): Future[Int]
  def getRating(userRatingId: java.util.UUID): Future[Option[UserRating]]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def getUserRatings(userId: java.util.UUID): Future[Seq[UserRating]]
  def getRatingsByMovie(movieId: Int): Future[Seq[UserRating]]
}

class UserRateMovieRepository(db: Database)
    extends UserRateMovieRepositoryComponent {
  protected val table = UserRateMovieTable
  import table.profile.api._
  import table.UserRateMovieRows

  override def add(userRating: UserRating): Future[UserRating] = db.run {
    (UserRateMovieRows returning UserRateMovieRows) += userRating
  }

  override def deleteRating(userMovie: UserMovie): Future[Int] = db.run {
    UserRateMovieRows
      .filter(row =>
        row.userId === userMovie.userId && row.movieId === userMovie.movieId
      )
      .delete
  }

  override def getRating(userRatingId: UUID): Future[Option[UserRating]] =
    db.run {
      UserRateMovieRows.filter(_.id === userRatingId).result.headOption
    }

  override def editRating(
      userRating: APIUserRating
  ): Future[Int] =
    db.run {
      UserRateMovieRows
        .filter(r =>
          r.userId === userRating.userId && r.movieId === userRating.movieId
        )
        .map(_.rating)
        .update(Some(userRating.rating))
    }

  override def deleteUser(userId: java.util.UUID): Future[Int] = db.run {
    UserRateMovieRows.filter(_.userId === userId).delete
  }

  override def deleteMovie(movieId: Int): Future[Int] = db.run {
    UserRateMovieRows.filter(_.movieId === movieId).delete
  }

  override def getUserRatings(
      userId: java.util.UUID
  ): Future[Seq[UserRating]] =
    db.run {
      UserRateMovieRows.filter(_.userId === userId).result
    }

  override def getRatingsByMovie(movieId: Int): Future[Seq[UserRating]] =
    db.run {
      UserRateMovieRows.filter(_.movieId === movieId).result
    }
}
