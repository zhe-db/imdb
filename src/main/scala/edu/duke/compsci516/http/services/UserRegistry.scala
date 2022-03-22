package edu.duke.compsci516.http.services

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

import edu.duke.compsci516.models.entity._
import edu.duke.compsci516.components.DatabaseComponent
import edu.duke.compsci516.models.components.UserRepository
import edu.duke.compsci516.models.components.UserRateMovieRepository

object UserRegistry extends DatabaseComponent {
  // actor protocol
  private val userRepo = new UserRepository(this.db)
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

  final case class GetUserResponse(maybeUser: Option[User])
  final case class CreateUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  final case class CreateUserMovieRatingResponse(
      maybeRating: Option[UserRating]
  )
  final case class EditUserMovieRatingResponse(
      maybeRating: Option[APIUserRating]
  )

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
        println(user)
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
    }
}
//#user-registry-actor
