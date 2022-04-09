package edu.duke.imdb.core.services
//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.sql.Timestamp
import java.time.Instant

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.components.DatabaseComponent
import _root_.edu.duke.imdb.models.components.UserRepository
import _root_.edu.duke.imdb.models.components.UserRateMovieRepository
import _root_.edu.duke.imdb.models.components.UserFavouriteMovieRepository

object UserRegistry extends DatabaseComponent {
  // actor protocol
  private val userRepo = new UserRepository(this.db)
  private val userFavMovieRepo = new UserFavouriteMovieRepository(this.db)
  private val userRatingRepo = new UserRateMovieRepository(this.db)

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(user: User, replyTo: ActorRef[CreateUserResponse])
      extends Command

  final case class GetUser(email: String, replyTo: ActorRef[GetUserResponse])
      extends Command

  final case class DeleteUser(email: String, replyTo: ActorRef[ActionPerformed])
      extends Command

  final case class UpdateUserLastLogin(
      userId: java.util.UUID,
      replyTo: ActorRef[ActionPerformed]
  ) extends Command

  final case class EditUserMovieRating(
      rating: APIUserRating,
      replyTo: ActorRef[EditUserMovieRatingResponse]
  ) extends Command

  final case class RateMovie(
      userRating: UserRating,
      replyTo: ActorRef[CreateUserMovieRatingResponse]
  ) extends Command

  final case class DeleteUserRating(
      userMovie: UserMovie,
      replyTo: ActorRef[DeleteUserRatingResponse]
  ) extends Command

  final case class GetUserFavGenres(
      userId: java.util.UUID,
      replyTo: ActorRef[GetUserFavGenresResponse]
  ) extends Command

  final case class GetUserFavMovies(
      userId: java.util.UUID,
      searchKey: String,
      limit: Int,
      page: Int,
      replyTo: ActorRef[GetUserFavMoviesResponse]
  ) extends Command

  final case class GetUserInfo(
      userId: java.util.UUID,
      replyTo: ActorRef[GetUserInfoResponse]
  ) extends Command

  final case class GetUserResponse(maybeUser: Option[User])

  final case class CreateUserResponse(maybeUser: Option[User])

  final case class ActionPerformed(description: String)

  final case class CreateUserMovieRatingResponse(
      maybeRating: Option[UserRating]
  )

  final case class DeleteUserRatingResponse(rows: Int)

  final case class EditUserMovieRatingResponse(
      maybeRating: Option[APIUserRating]
  )

  final case class GetUserFavGenresResponse(genreCounts: GenreCounts)

  final case class GetUserFavMoviesResponse(
      movies: PaginatedResult[MovieDetailRow]
  )

  final case class GetUserInfoResponse(maybeUser: Option[UserInfo])

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        userRepo.getUsers().onComplete {
          case Success(users) =>
            replyTo ! Users(users.toSeq)
          case Failure(f) => replyTo ! Users(Seq.empty[User])
        }
        Behaviors.same

      case CreateUser(user, replyTo) =>
        userRepo.add(user).onComplete {
          case Success(_) =>
            replyTo ! CreateUserResponse(Some(user))
          case Failure(f) => CreateUserResponse(None)
        }
        Behaviors.same

      case GetUser(email, replyTo) =>
        userRepo.get(email).onComplete {
          case Success(user) => replyTo ! GetUserResponse(user)
          case Failure(f)    => replyTo ! GetUserResponse(None)
        }
        Behaviors.same

      case DeleteUser(email, replyTo) =>
        userRepo.deleteBy(email).onComplete {
          case Success(rows) =>
            replyTo ! ActionPerformed(s"User ${email} deleted.")
          case Failure(f) => replyTo ! ActionPerformed(s"Failed: ${f}")
        }
        Behaviors.same

      case UpdateUserLastLogin(userId, replyTo) =>
        userRepo
          .updateLastLogin(userId, Timestamp.from(Instant.now()))
          .onComplete {
            case Success(rows) =>
              replyTo ! ActionPerformed(s"User ${userId} last login updated.")
            case Failure(f) => replyTo ! ActionPerformed(s"Failed: ${f}")
          }
        Behaviors.same

      case GetUserInfo(userId, replyTo) =>
        userRepo.getUserInfo(userId).onComplete {
          case Success(Some(user)) =>
            replyTo ! GetUserInfoResponse(
              Some(new UserInfo(user.name, user.createdOn))
            )
          case _ =>
            replyTo ! GetUserInfoResponse(None)
        }
        Behaviors.same

      case RateMovie(userRating, replyTo) =>
        userRatingRepo.add(userRating).onComplete {
          case Success(rating) =>
            replyTo ! CreateUserMovieRatingResponse(Some(rating))
          case Failure(f) =>
            replyTo ! CreateUserMovieRatingResponse(None)
        }
        Behaviors.same

      case EditUserMovieRating(userRating, replyTo) =>
        userRatingRepo.editRating(userRating).onComplete {
          case Success(rating) =>
            replyTo ! EditUserMovieRatingResponse(Some(userRating))
          case Failure(f) =>
            replyTo ! EditUserMovieRatingResponse(None)
        }
        Behaviors.same

      case GetUserFavGenres(userId, replyTo) =>
        userFavMovieRepo.getUserFavouriteGenres(userId).onComplete {
          case Success(genreCounts) =>
            replyTo ! GetUserFavGenresResponse(
              new GenreCounts(
                genreCounts.map(x =>
                  new GenreCount(new Genre(id = x._1._1, name = x._1._2), x._2)
                )
              )
            )
          case Failure(f) =>
            replyTo ! GetUserFavGenresResponse(
              new GenreCounts(Seq.empty[GenreCount])
            )
        }
        Behaviors.same

      case GetUserFavMovies(userId, searchKey, limit, page, replyTo) =>
        userFavMovieRepo
          .getUserFavouriteMovies(userId, searchKey, limit, limit * page)
          .onComplete {
            case Success(movies) =>
              replyTo ! GetUserFavMoviesResponse(movies)
            case Failure(f) =>
              replyTo ! GetUserFavMoviesResponse(
                new PaginatedResult[MovieDetailRow](
                  0,
                  List.empty[MovieDetailRow],
                  false
                )
              )
          }
        Behaviors.same

      case DeleteUserRating(userMovie, replyTo) =>
        userRatingRepo.deleteRating(userMovie).onComplete {
          case Success(rows) =>
            replyTo ! DeleteUserRatingResponse(rows)
          case Failure(f) =>
            replyTo ! DeleteUserRatingResponse(0)
        }
        Behaviors.same
    }
}
//#user-registry-actor
