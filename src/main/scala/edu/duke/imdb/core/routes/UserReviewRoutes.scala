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

  def addMovieReview(review: UserReview): Future[GetUserMovieReviewResponse] =
    userReviewActor.ask(AddUserMovieReview(review, _))

  def editMovieReview(review: APIUserReview): Future[StatusResponse] =
    userReviewActor.ask(EditUserMovieReview(review, _))

  def deleteUserReview(reviewId: java.util.UUID): Future[StatusResponse] =
    userReviewActor.ask(DeleteUserMovieReviewById(reviewId, _))

  def getUserReviews(
      userId: java.util.UUID
  ): Future[GetUserMovieReviewsResponse] =
    userReviewActor.ask(GetUserMovieReviews(userId, _))

  def deleteReviewsByUser(userId: java.util.UUID): Future[StatusResponse] =
    userReviewActor.ask(DeleteReviewsByUser(userId, _))

  def deleteReviewsByMovie(movieId: Int): Future[StatusResponse] =
    userReviewActor.ask(DeleteMovieReviewByMovie(movieId, _))

  def getReviewsByMovie(movieId: Int): Future[GetUserMovieReviewsResponse] =
    userReviewActor.ask(GetMovieReviewsByMovie(movieId, _))

  val reviewRoutes: Route = concat(
    pathPrefix("list") {
      concat(
        path("user") {
          pathEnd {
            concat(
              get {
                parameters("userId".as[java.util.UUID]) { userId =>
                  onSuccess(getUserReviews(userId)) { response =>
                    complete((StatusCodes.OK, response.maybeReviews))
                  }
                }
              },
              delete {
                parameters("userId".as[java.util.UUID]) { userId =>
                  onSuccess(deleteReviewsByUser(userId)) { response =>
                    complete((StatusCodes.OK, s"${response.success}"))
                  }
                }
              }
            )
          }
        },
        path("movie") {
          pathEnd {
            concat(
              get {
                parameters("movieId".as[Int]) { movieId =>
                  onSuccess(getReviewsByMovie(movieId)) { response =>
                    complete((StatusCodes.OK, response.maybeReviews))
                  }
                }
              },
              delete {
                parameters("movieId".as[Int]) { movieId =>
                  onSuccess(deleteReviewsByMovie(movieId)) { response =>
                    complete((StatusCodes.OK, s"${response.success}"))
                  }
                }
              }
            )
          }
        }
      )
    },
    pathEnd {
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
              complete((StatusCodes.Created, response.maybeReview))
            }
          }
        },
        put {
          entity(as[APIUserReview]) { userReview =>
            onSuccess(editMovieReview(userReview)) { response =>
              complete((StatusCodes.OK, s"${response.success}"))
            }
          }
        },
        delete {
          parameters("reviewId".as[java.util.UUID]) { reviewId =>
            onSuccess(deleteUserReview(reviewId)) { response =>
              complete((StatusCodes.OK, s"${response.success}"))
            }
          }
        }
      )
    }
  )
}
