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

import edu.duke.compsci516.models.entity.{User, Users}
import edu.duke.compsci516.components.DatabaseComponent
import edu.duke.compsci516.models.components.UserRepository

object UserRegistry extends DatabaseComponent {
  // actor protocol
  private val userRepo = new UserRepository(this.db)

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

  final case class GetUserResponse(maybeUser: Option[User])
  final case class CreateUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry()

  private def registry(): Behavior[Command] =
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
        registry()
      case GetUser(email, replyTo) =>
        userRepo.get(email).onComplete {
          case Success(user) => replyTo ! GetUserResponse(user)
          case Failure(f)    => replyTo ! GetUserResponse(None)
        }
        // replyTo ! GetUserResponse(users.find(_.name == name))
        Behaviors.same
      case DeleteUser(email, replyTo) =>
        userRepo.deleteBy(email).onComplete {
          case Success(rows) =>
            replyTo ! ActionPerformed(s"User ${email} deleted.")
          case Failure(f) => replyTo ! ActionPerformed(s"Failed: ${f}")
        }
        registry()
      case UpdateUserLastLogin(userId, replyTo) =>
        userRepo
          .updateLastLogin(userId, Timestamp.from(Instant.now()))
          .onComplete {
            case Success(rows) =>
              replyTo ! ActionPerformed(s"User ${userId} last login updated.")
            case Failure(f) => replyTo ! ActionPerformed(s"Failed: ${f}")
          }
        registry()
    }
}
//#user-registry-actor
