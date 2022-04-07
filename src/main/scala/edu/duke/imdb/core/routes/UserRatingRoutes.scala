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
import _root_.edu.duke.imdb.core.services.UserRatingActor._
import _root_.edu.duke.imdb.core.services.Authenticator
import _root_.edu.duke.imdb.models.entity.UserRating
import akka.actor.Status

class UserRatingRoutes(
    userRatingActor: ActorRef[UserRatingActor.Command]
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

  def createUserRating(
      rating: UserRating
  ): Future[GetRatingResponse] =
    userRatingActor.ask(AddUserRating(rating, _))

  def getRatingById(ratingId: java.util.UUID): Future[GetRatingResponse] =
    userRatingActor.ask(GetRating(ratingId, _))

  def deleteUserRating(ratingId: java.util.UUID): Future[StatusResponse] =
    userRatingActor.ask(DeleteRatingById(ratingId, _))

  def editUserRating(
      rating: APIUserRating
  ): Future[StatusResponse] =
    userRatingActor.ask(EditUserRating(rating, _))

  def getRatingByMovie(movieId: Int): Future[GetRatingsResponse] =
    userRatingActor.ask(GetMovieRatingByMovie(movieId, _))

  def deleteRatingByMovie(movieId: Int): Future[StatusResponse] =
    userRatingActor.ask(DeleteRatingByMovie(movieId, _))

  def getUserRatings(userId: java.util.UUID): Future[GetRatingsResponse] =
    userRatingActor.ask(GetUserRating(userId, _))

  def deleteRatingsByUser(userId: java.util.UUID): Future[StatusResponse] =
    userRatingActor.ask(DeleteRatingByUser(userId, _))

  val ratingRoutes: Route = concat(
    pathPrefix("list") {
      concat(
        path("user") {
          pathEnd {
            concat(
              get {
                parameters("userId".as[java.util.UUID]) { userId =>
                  onSuccess(getUserRatings(userId)) { response =>
                    complete((StatusCodes.OK, response.maybeRatings))
                  }
                }
              },
              delete {
                parameters("userId".as[java.util.UUID]) { userId =>
                  onSuccess(deleteRatingsByUser(userId)) { response =>
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
                  onSuccess(getRatingByMovie(movieId)) { response =>
                    complete((StatusCodes.OK, response.maybeRatings))
                  }
                }
              },
              delete {
                parameters("movieId".as[Int]) { movieId =>
                  onSuccess(deleteRatingByMovie(movieId)) { response =>
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
          parameters("ratingId".as[java.util.UUID]) { ratingId =>
            onSuccess(getRatingById(ratingId)) { response =>
              complete((StatusCodes.OK, response.maybeRating))
            }
          }
        },
        post {
          entity(as[APIUserRating]) { rating =>
            onSuccess(createUserRating(rating.toUserRating())) { response =>
              complete((StatusCodes.Created, response.maybeRating))
            }
          }
        },
        put {
          entity(as[APIUserRating]) { rating =>
            onSuccess(editUserRating(rating)) { response =>
              complete((StatusCodes.OK, s"${response.success}"))
            }
          }
        },
        delete {
          parameters("ratingId".as[java.util.UUID]) { ratingId =>
            onSuccess(deleteUserRating(ratingId)) { response =>
              complete(
                (StatusCodes.OK, s"${response.success}")
              )
            }
          }
        }
      )
    }
  )
}
