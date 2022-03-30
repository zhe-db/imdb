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
import edu.duke.imdb.components._
import edu.duke.imdb.models.components._

object GenreRegistry extends DatabaseComponent {
  private val genreRepo = new GenreRepository(this.db)
  private val movieRepo = new MovieRepository(this.db)

  sealed trait Command
  final case class GetGenres(replyTo: ActorRef[Genres]) extends Command
  final case class GetGenre(genreId: Int, replyTo: ActorRef[GetGenreResopnse])
      extends Command
  final case class AddGenre(genre: Genre, replyTo: ActorRef[AddGenreResponse])
      extends Command
  final case class DeleteGenre(genreId: Int, replyTo: ActorRef[ActionPerformed])
      extends Command
  final case class UpdateGenre(
      genre: Genre,
      replyTo: ActorRef[ActionPerformed]
  ) extends Command

  final case class GetMoviesByGenre(
      genreId: Int,
      sortKey: String,
      limit: Int,
      page: Int,
      replyTo: ActorRef[GetMoviesByGenreResponse]
  ) extends Command

  final case class GetGenreResopnse(maybeGenre: Option[Genre])
  final case class AddGenreResponse(maybeGenre: Option[Genre])
  final case class ActionPerformed(description: String)
  final case class GetMoviesByGenreResponse(
      movies: PaginatedResult[MovieDetailRow]
  )

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetGenres(replyTo) =>
        genreRepo.getGenres().onComplete {
          case Success(genres) =>
            replyTo ! Genres(genres)
          case Failure(f) => replyTo ! Genres(Seq.empty[Genre])
        }
        Behaviors.same

      case GetGenre(genreId: Int, replyTo: ActorRef[GetGenreResopnse]) =>
        genreRepo.getGenre(genreId).onComplete {
          case Success(genre) =>
            replyTo ! GetGenreResopnse(genre)
          case Failure(f) =>
            replyTo ! GetGenreResopnse(None)
        }
        Behaviors.same

      case DeleteGenre(genreId: Int, replyTo: ActorRef[ActionPerformed]) =>
        genreRepo.delete(genreId).onComplete {
          case Success(rows) =>
            replyTo ! ActionPerformed(s"${genreId} deleted.")
          case Failure(f) =>
            replyTo ! ActionPerformed(s"Failed to delete ${genreId}.")
        }
        Behaviors.same

      case UpdateGenre(genre: Genre, replyTo: ActorRef[ActionPerformed]) =>
        genreRepo.update(genre).onComplete {
          case Success(rows) =>
            replyTo ! ActionPerformed(s"Updated ${genre}.")
          case Failure(f) =>
            replyTo ! ActionPerformed(s"Failed to update ${genre}.")
        }
        Behaviors.same

      case AddGenre(genre: Genre, replyTo: ActorRef[AddGenreResponse]) =>
        genreRepo.add(genre).onComplete {
          case Success(rows) =>
            replyTo ! AddGenreResponse(Some(genre))
          case Failure(f) =>
            replyTo ! AddGenreResponse(None)
        }
        Behaviors.same

      case GetMoviesByGenre(
            genreId: Int,
            sortKey: String,
            limit: Int,
            page: Int,
            replyTo: ActorRef[GetMoviesByGenreResponse]
          ) =>
        movieRepo
          .getMoviesByGenre(genreId, sortKey, limit, page * limit)
          .onComplete {
            case Success(movies) =>
              replyTo ! GetMoviesByGenreResponse(movies)
            case Failure(f) =>
              replyTo ! GetMoviesByGenreResponse(
                new PaginatedResult(0, List.empty[MovieDetailRow], false)
              )
          }
        Behaviors.same
    }
}
