package edu.duke.imdb.core.services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.components.DatabaseComponent
import _root_.edu.duke.imdb.models.components.UserReviewMovieRepository

object UserReviewActor extends DatabaseComponent {
  val userReviewMovieRepo = new UserReviewMovieRepository(this.db)

  sealed trait Command

  final case class AddUserMovieReview(
      userReview: UserReview,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class EditUserMovieReview(
      userReview: APIUserReview,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class GetUserMovieReviews(
      userId: java.util.UUID,
      replyTo: ActorRef[GetUserMovieReviewsResponse]
  ) extends Command

  final case class GetMovieReviewsByMovie(
      movieId: Int,
      replyTo: ActorRef[GetUserMovieReviewsResponse]
  ) extends Command

  final case class GetMovieReview(
      reviewId: java.util.UUID,
      replyTo: ActorRef[GetUserMovieReviewResponse]
  ) extends Command

  final case class DeleteUserMovieReviewById(
      reviewId: java.util.UUID,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class DeleteMovieReviewByMovie(
      movieId: Int,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class DeleteUserMovieReview(
      userMovie: UserMovie,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class DeleteReviewsByUser(
      userId: java.util.UUID,
      replyTo: ActorRef[StatusResponse]
  ) extends Command

  final case class GetUserMovieReviewResponse(maybeReview: Option[UserReview])

  final case class GetUserMovieReviewsResponse(maybeReviews: UserReviews)

  final case class StatusResponse(success: Boolean)

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {

      case GetMovieReview(reviewId, replyTo) =>
        userReviewMovieRepo.getMovieReview(reviewId = reviewId).onComplete {
          case Success(review) =>
            replyTo ! GetUserMovieReviewResponse(review)
          case Failure(f) =>
            replyTo ! GetUserMovieReviewResponse(None)
        }
        Behaviors.same

      case AddUserMovieReview(userReview, replyTo) =>
        userReviewMovieRepo.add(userReview).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case GetUserMovieReviews(userId, replyTo) =>
        userReviewMovieRepo.getUserReviews(userId).onComplete {
          case Success(reviews) =>
            replyTo ! GetUserMovieReviewsResponse(UserReviews(reviews))
          case Failure(f) =>
            replyTo ! GetUserMovieReviewsResponse(
              UserReviews(Seq.empty[UserReview])
            )
        }
        Behaviors.same

      case EditUserMovieReview(userReview, replyTo) =>
        userReviewMovieRepo.edit(userReview).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case DeleteUserMovieReviewById(reviewId, replyTo) =>
        userReviewMovieRepo.deleteReview(reviewId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case DeleteMovieReviewByMovie(movieId, replyTo) =>
        userReviewMovieRepo.deleteMovie(movieId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case DeleteUserMovieReview(reviewId, replyTo) =>
        userReviewMovieRepo.deleteReview(reviewId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

      case GetMovieReviewsByMovie(movieId, replyTo) =>
        userReviewMovieRepo.getReviewsByMovie(movieId).onComplete {
          case Success(reviews) =>
            replyTo ! GetUserMovieReviewsResponse(UserReviews(reviews))
          case Failure(f) =>
            replyTo ! GetUserMovieReviewsResponse(
              UserReviews(Seq.empty[UserReview])
            )
        }
        Behaviors.same

      case DeleteReviewsByUser(userId, replyTo) =>
        userReviewMovieRepo.deleteUser(userId).onComplete {
          case Success(rows) =>
            replyTo ! StatusResponse(true)
          case Failure(f) =>
            replyTo ! StatusResponse(false)
        }
        Behaviors.same

    }
}
