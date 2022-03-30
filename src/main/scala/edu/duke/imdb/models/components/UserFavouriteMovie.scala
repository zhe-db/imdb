package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.database._
import _root_.edu.duke.imdb.models.entity._
import scala.concurrent.ExecutionContext

trait UserFavouriteMovieRepositoryComponent {
  def add(userFavMovie: UserFavouriteMovie): Future[Int]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteMovie(movieId: Int): Future[Int]
  def getUserFavouriteMovies(userId: java.util.UUID): Future[Seq[Option[Int]]]
  def getUsersByMovie(movieId: Int): Future[Seq[java.util.UUID]]
  def getUserFavouriteGenres(
      userId: java.util.UUID
  ): Future[Seq[((Int, String), Int)]]
  def getUserFavouriteMovies(
      userId: java.util.UUID,
      searchKey: String,
      limit: Int,
      offset: Int
  ): Future[PaginatedResult[MovieDetailRow]]
}

class UserFavouriteMovieRepository(db: Database)
    extends UserFavouriteMovieRepositoryComponent {
  protected val table = UserFavouriteMovieTable
  protected val movieGenresTable = MovieGenreTable
  protected val genresTable = GenreTable
  protected val movieDetailTable = MovieDetailTable

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  import table.profile.api._
  import table.UserFavouriteMovieRows
  import movieGenresTable.MovieGenreRows
  import genresTable.GenreRows
  import movieDetailTable.MovieDetailRows

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

  override def getUserFavouriteMovies(
      userId: java.util.UUID,
      sortKey: String,
      limit: Int,
      offset: Int
  ): Future[PaginatedResult[MovieDetailRow]] = db.run {
    val q = UserFavouriteMovieRows
      .filter(_.userId === userId)
      .join(MovieDetailRows)
      .on(_.movieId === _.id)
      .map(_._2)

    val q_sort = sortKey match {
      case "popularity.desc"   => q.sortBy(_.popularity.desc)
      case "vote_average.desc" => q.sortBy(_.voteAverage.desc)
      case "vote_count.desc"   => q.sortBy(_.voteCount.desc)
      case "title.desc"        => q.sortBy(_.title.desc)
      case "release_date.desc" => q.sortBy(_.releaseDate.desc)
      case _                   => q
    }

    for {
      numberOfRows <- q_sort.length.result
      res <- q_sort.drop(offset).take(limit).result
    } yield PaginatedResult(
      totalCount = numberOfRows,
      entities = res.toList,
      hasNextPage = numberOfRows - (offset + limit) > 0
    )
  }

  override def getUserFavouriteGenres(
      userId: java.util.UUID
  ): Future[Seq[((Int, String), Int)]] =
    db.run {
      UserFavouriteMovieRows
        .filter(_.userId === userId)
        .map(_.movieId)
        .join(MovieGenreRows)
        .on(_ === _.movieId)
        .map(_._2)
        .join(GenreRows)
        .on(_.genreId === _.id)
        .map(x => (x._2.id, x._2.name, 1))
        .groupBy { x => (x._1, x._2) }
        .map { case ((a, b), c) => ((a, b), c.length) }
        .result
    }
}
