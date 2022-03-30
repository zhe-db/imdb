package edu.duke.imdb.core.services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.sql.Timestamp
import java.time.Instant

import edu.duke.imdb.models.entity._
import edu.duke.imdb.components.DatabaseComponent
import edu.duke.imdb.models.entity.MovieDetailRow
import edu.duke.imdb.models.components.MovieRepository
import edu.duke.imdb.models.components.UserFavouriteMovieRepository
import edu.duke.imdb.models.components.UserRateMovieRepository
import edu.duke.imdb.models.components.UserReviewMovieRepository

object MovieRegistry extends DatabaseComponent {
  private val movieRepo = new MovieRepository(this.db)
  private val userFavMovieRepo = new UserFavouriteMovieRepository(this.db)
  private val userRatingRepo = new UserRateMovieRepository(this.db)
  private val userReviewRepo = new UserReviewMovieRepository(this.db)

  sealed trait Command
  final case class GetMovie(
      movidId: Integer,
      replyTo: ActorRef[GetMovieResponse]
  ) extends Command

  final case class GetCompleteMovie(
      movieId: Integer,
      replyTo: ActorRef[CompleteMovieResponse]
  ) extends Command

  final case class AddMovie(
      movie: MovieDetailRow,
      replyTo: ActorRef[AddMovieResponse]
  ) extends Command

  final case class LikeMovie(
      likeMovie: APIUserFavouriteMovie,
      replyTo: ActorRef[LikeMovieResponse]
  ) extends Command

  final case class RateMovie(
      userRating: APIUserRating,
      replyTo: ActorRef[RateMovieResponse]) extends Command

  final case class ReviewMovie(userReview: APIUserReview,
    replyTo: ActorRef[ReviewMovieResponse]) extends Command

  final case class GetMovieResponse(maybeMovie: Option[MovieDetailRow])
      extends Command

  final case class AddMovieResponse(maybeMovie: Option[MovieDetailRow])

  final case class CompleteMovieResponse(maybeMovie: Option[CompleteMovie])

  final case class LikeMovieResponse(result: String)

  final case class RateMovieResponse(result: String)

  final case class ReviewMovieResponse(result: String)

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case AddMovie(movie, replyTo) =>
        movieRepo.add(movie).onComplete {
          case Success(mov_res) =>
            replyTo ! new AddMovieResponse(Some(mov_res))
          case Failure(f) =>
            replyTo ! new AddMovieResponse(None)
        }
        Behaviors.same

      case GetCompleteMovie(movieId, replyTo) =>
        getCompleteMovie(movieId, replyTo)
        Behaviors.same

      case LikeMovie(likeMovie, replyTo) =>
        userFavMovieRepo.add(likeMovie.toUserFavouriteMovie()).onComplete {
          case Success(res) =>
            replyTo ! new LikeMovieResponse("Successful")
          case Failure(f) =>
            replyTo ! new LikeMovieResponse(f.toString())
        }
        Behaviors.same

      case RateMovie(userRating, replyTo) =>
        userRatingRepo.add(userRating.toUserRating()).onComplete {
        case Success(res) =>
          replyTo ! new RateMovieResponse("Successful")
        case Failure(f) =>
          replyTo ! new RateMovieResponse(f.toString())
        }
        Behaviors.same

      case ReviewMovie(userReview, replyTo) =>
        userReviewRepo.add(userReview.toUserReview()).onComplete {
          case Success(res) =>
            replyTo ! new ReviewMovieResponse("Successful")
          case Failure(f) =>
            replyTo ! new ReviewMovieResponse(f.toString())
        }
        Behaviors.same
    }

  def getCompleteMovie(
      movieId: Int,
      replyTo: ActorRef[CompleteMovieResponse]
  ): Unit = {
    movieRepo.get(movieId).onComplete {
      case Success(Some(movie)) => {
        var completeMovie = new CompleteMovie(
          movie,
          Genres(Seq.empty[Genre]),
          MovieCrews(Seq.empty[MovieCrew])
        )
        movieRepo.getGenres(movieId).onComplete {
          case Success(genres) => {
            completeMovie.genres = Genres(genres)
            movieRepo.getCrews(movieId).onComplete {
              case Success(movieCrews) => {
                completeMovie.crews = MovieCrews(movieCrews)
                replyTo ! new CompleteMovieResponse(Some(completeMovie))
              }
              case Failure(f) =>
                replyTo ! new CompleteMovieResponse(Some(completeMovie))
            }
          }
          case Failure(f) => {
            movieRepo.getCrews(movieId).onComplete {
              case Success(movieCrews) => {
                completeMovie.crews = MovieCrews(movieCrews)
                replyTo ! new CompleteMovieResponse(Some(completeMovie))
              }
              case Failure(f) =>
                replyTo ! new CompleteMovieResponse(Some(completeMovie))
            }
          }
        }
      }
      case Failure(f) => replyTo ! new CompleteMovieResponse(None)
    }
  }
}
