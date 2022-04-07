package edu.duke.imdb.core.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.core.services._
import _root_.edu.duke.imdb.core.services.UserReviewActor._
import _root_.edu.duke.imdb.core.services.Authenticator
import _root_.edu.duke.imdb.models.entity.UserRating
import akka.actor.Status

class UserReviewRoutes(
    userReviewActor: ActorRef[UserReviewActor.Command]
)(implicit
    val system: ActorSystem[_]
) {

  // #user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  // #import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(
    system.settings.config.getDuration("my-app.routes.ask-timeout")
  )

  def getMovieReview(
      reviewId: java.util.UUID
  ): Future[GetUserMovieReviewResponse] =
    userReviewActor.ask(GetMovieReview(reviewId, _))

  def addMovieReview(review: UserReview): Future[StatusResponse] =
    userReviewActor.ask(AddUserMovieReview(review, _))

  def editMovieReview(review: APIUserReview): Future[StatusResponse] = userReviewActor.ask(EditUserMovieReview(review, _))

  val reviewRoutes: Route = concat(
    concat(
      get {
        parameters("reviewId".as[java.util.UUID]) { reviewId =>
          onSuccess(getMovieReview(reviewId)) { response =>
            complete((StatusCodes.OK, response.maybeReview))
          }
        }
      },
      post {
        entity(as[APIUserReview]) { userReview =>
          onSuccess(addMovieReview(userReview.toUserReview())) { response =>
            complete((StatusCodes.Created, s"${response.success}"))
          }
        }
      },
      put {
        entity(as[APIUserReview]) { userReview =>
          onSuccess(editMovieReview(userReview)) { response =>
            complete((StatusCodes.OK, s"${response.success}"))
          }
        }
      }
    )
  )
}
