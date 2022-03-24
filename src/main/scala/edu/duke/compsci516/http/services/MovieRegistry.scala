package edu.duke.compsci516.http.services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.sql.Timestamp
import java.time.Instant

import edu.duke.compsci516.models.entity._
import edu.duke.compsci516.components.DatabaseComponent
import edu.duke.compsci516.models.entity.MovieDetailRow
import edu.duke.compsci516.models.components.MovieRepository

object MovieRegistry extends DatabaseComponent {
  private val movieRepo = new MovieRepository(this.db)

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

  final case class GetMovieResponse(maybeMovie: Option[MovieDetailRow])
      extends Command

  final case class AddMovieResponse(maybeMovie: Option[MovieDetailRow])

  final case class CompleteMovieResponse(maybeMovie: Option[CompleteMovie])

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
