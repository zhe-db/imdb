package edu.duke.imdb.core.services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.components.DatabaseComponent
import _root_.edu.duke.imdb.models.components.UserRateMovieRepository

object UserRatingActor extends DatabaseComponent {
  val userRatingRepo = new UserRateMovieRepository(this.db)

  sealed trait Command

  final case class AddUserRating(
      userRating: UserRating,
      replyTo: ActorRef[GetRatingResponse]
  ) extends Command

  final case class EditUserRating(
      userReview: APIUserRating,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class GetUserRating(
      userId: java.util.UUID,
      replyTo: ActorRef[GetRatingsResponse]
  ) extends Command

  final case class GetMovieRatingByMovie(
      movieId: Int,
      replyTo: ActorRef[GetRatingsResponse]
  ) extends Command

  final case class GetRating(
      reviewId: java.util.UUID,
      replyTo: ActorRef[GetRatingResponse]
  ) extends Command

  final case class DeleteRatingById(
      reviewId: java.util.UUID,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class DeleteRatingByMovie(
      movieId: Int,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class DeleteRatingByUser(
      userId: java.util.UUID,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class GetRatingResponse(maybeRating: Option[UserRating])

  final case class GetRatingsResponse(maybeRatings: UserRatings)

  final case class StatusResponse(success: Boolean)

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {

      case GetRating(ratingId, replyTo) =>
        userRatingRepo.getRating(ratingId).onComplete {
          case Success(rating) =>
            replyTo ! GetRatingResponse(rating)
          case Failure(f) =>
            replyTo ! GetRatingResponse(None)
        }
        Behaviors.same

      case AddUserRating(userRating, replyTo) =>
        userRatingRepo.add(userRating).onComplete {
          case Success(rows) =>
            replyTo ! GetRatingResponse(Some(userRating))
          case Failure(f) =>
            replyTo ! GetRatingResponse(None)
        }
        Behaviors.same

      case GetUserRating(userId, replyTo) =>
        userRatingRepo.getUserRatings(userId).onComplete {
          case Success(ratings) =>
            replyTo ! GetRatingsResponse(UserRatings(ratings))
          case Failure(f) =>
            replyTo ! GetRatingsResponse(
              UserRatings(Seq.empty[UserRating])
            )
        }
        Behaviors.same

      case EditUserRating(userRating, replyTo) =>
        userRatingRepo.editRating(userRating).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case DeleteRatingById(ratingId, replyTo) =>
        userRatingRepo.deleteRating(ratingId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case DeleteRatingByMovie(movieId, replyTo) =>
        userRatingRepo.deleteMovie(movieId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case GetMovieRatingByMovie(movieId, replyTo) =>
        userRatingRepo.getRatingsByMovie(movieId).onComplete {
          case Success(rating) =>
            replyTo ! GetRatingsResponse(UserRatings(rating))
          case Failure(f) =>
            replyTo ! GetRatingsResponse(
              UserRatings(Seq.empty[UserRating])
            )
        }
        Behaviors.same

      case DeleteRatingByUser(userId, replyTo) =>
        userRatingRepo.deleteUser(userId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

    }
}
