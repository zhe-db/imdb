package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.models.database._
import scala.util.Success
import scala.concurrent.ExecutionContext
import slick.basic.DatabasePublisher
import slick.jdbc.ResultSetType
import slick.jdbc.ResultSetConcurrency
import java.util.UUID

trait MovieRecommendationRepositoryComponent {
  def allMoviesWithGenresAndCrews()
      : DatabasePublisher[(MovieDetailRow, Int, Int)]
  def allMoviesWithUserRatings(): DatabasePublisher[(Int, Int, Option[Double])]
  def moviesRatingByUser(
      userId: java.util.UUID
  ): DatabasePublisher[(Option[Int], Option[Double])]
}

class MovieRecommendationRepository(db: Database)
    extends MovieRecommendationRepositoryComponent {
  protected val movieDetailTable = MovieDetailTable
  protected val genreTable = GenreTable
  protected val movieGenreTable = MovieGenreTable
  protected val movieCrewTable = MovieCrewTable
  protected val crewTable = CrewTable
  protected val userRatingTable = UserRateMovieTable
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  import movieDetailTable.profile.api._
  import movieDetailTable.MovieDetailRows
  import genreTable.GenreRows
  import movieGenreTable.MovieGenreRows
  import movieCrewTable.MovieCrewRows
  import crewTable.CrewRows
  import userRatingTable.UserRateMovieRows

  override def allMoviesWithGenresAndCrews()
      : DatabasePublisher[(MovieDetailRow, Int, Int)] = {
    val query = (for {
      ((movie, genre), crew) <-
        MovieDetailRows join MovieGenreRows on (_.id === _.movieId) join MovieCrewRows on (_._1.id === _.movieId)
    } yield (movie, genre.genreId, crew.crewId))
    db.stream(query.result)
  }

  override def allMoviesWithUserRatings()
      : DatabasePublisher[(Int, Int, Option[Double])] = {
    val query = (for {
      (movie, rating) <-
        MovieDetailRows join UserRateMovieRows on (_.id === _.movieId)
    } yield (movie.id, rating.rating)).groupBy(_._1)
    val aggQuery = query.map { case (movieId, ratings) =>
      (movieId, ratings.length, ratings.map(_._2).avg)
    }
    db.stream(aggQuery.result)
  }

  override def moviesRatingByUser(
      userId: UUID
  ): DatabasePublisher[(Option[Int], Option[Double])] = {
    val query = UserRateMovieRows
      .filter(_.userId === userId)
      .map(rating => (rating.movieId, rating.rating))
    db.stream(query.result)
  }

}
