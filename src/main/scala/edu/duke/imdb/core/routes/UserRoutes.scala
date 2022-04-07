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
import _root_.edu.duke.imdb.core.services.UserRegistry._
import _root_.edu.duke.imdb.core.services.Authenticator
import _root_.edu.duke.imdb.models.entity.UserRating
import akka.actor.Status

class UserRoutes(
    userRegistry: ActorRef[UserRegistry.Command],
    userReviewRoutes: Route,
    userRatingRoutes: Route
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

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers)

  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))

  def createUser(user: User): Future[CreateUserResponse] =
    userRegistry
      .ask(CreateUser(user, _))

  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

  def updateUserLastLogin(userId: java.util.UUID): Future[ActionPerformed] =
    userRegistry.ask(UpdateUserLastLogin(userId, _))

  def createUserRating(
      rating: UserRating
  ): Future[CreateUserMovieRatingResponse] =
    userRegistry.ask(RateMovie(rating, _))

  def deleteUserRating(userMovie: UserMovie): Future[DeleteUserRatingResponse] =
    userRegistry.ask(DeleteUserRating(userMovie, _))

  def editUserRating(
      rating: APIUserRating
  ): Future[EditUserMovieRatingResponse] =
    userRegistry.ask(EditUserMovieRating(rating, _))

  def getUserFavouriteGenres(
      userId: java.util.UUID
  ): Future[GetUserFavGenresResponse] =
    userRegistry.ask(GetUserFavGenres(userId, _))

  def getUserFavouriteMovies(
      userId: java.util.UUID,
      searchKey: String,
      limit: Int,
      page: Int
  ): Future[GetUserFavMoviesResponse] =
    userRegistry.ask(GetUserFavMovies(userId, searchKey, limit, page, _))

  def getUserInfo(userId: java.util.UUID): Future[GetUserInfoResponse] =
    userRegistry.ask(GetUserInfo(userId, _))

  // #all-routes
  // #users-get-post
  // #users-get-delete
  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        // #users-get-delete
        pathEnd {
          concat(
            get {
              authenticateBasicAsync(
                realm = "secure",
                Authenticator.UserAuthenticatorAsync
              ) { user =>
                complete(getUsers())
              }
            },
            post {
              entity(as[APIUser]) { user =>
                onSuccess(createUser(user.toUser())) { response =>
                  complete((StatusCodes.Created, response.maybeUser))
                }
              }
            }
          )
        },
        // #users-get-delete
        // #users-get-post
        path("login") {
          get {
            authenticateBasicAsync(
              realm = "secure",
              Authenticator.UserAuthenticatorAsync
            ) { user =>
              updateUserLastLogin(user.userId)
              complete(user)
            }
          }
        },
        pathPrefix("favourite") {
          concat(
            path("genres") {
              pathEnd {
                get {
                  parameters("userId".as[java.util.UUID]) { (userId) =>
                    onSuccess(getUserFavouriteGenres(userId)) { response =>
                      complete((StatusCodes.OK, response.genreCounts))
                    }
                  }
                }
              }
            },
            path("movies") {
              pathEnd {
                get {
                  parameters(
                    "userId".as[java.util.UUID],
                    "sortKey".as[Option[String]],
                    "page".as[Int],
                    "limit".as[Int]
                  ) { (userId, sortKey, page, limit) =>
                    onSuccess(
                      getUserFavouriteMovies(
                        userId,
                        sortKey.getOrElse(""),
                        limit,
                        page
                      )
                    ) { response =>
                      complete((StatusCodes.OK, response.movies))
                    }
                  }
                }
              }
            },
            path("actors") {
              pathEnd {
                get {
                  parameters("userId".as[java.util.UUID]) { (userId) =>
                    complete((StatusCodes.OK, "success"))
                  }
                }
              }
            }
          )
        },
        pathPrefix("info") {
          pathEnd {
            get {
              parameters("userId".as[java.util.UUID]) { (userId) =>
                onSuccess(getUserInfo(userId)) { response =>
                  complete((StatusCodes.OK, response.maybeUser))
                }
              }
            }
          }
        },
        path(Segment) { email =>
          concat(
            get {
              // #retrieve-user-info
              rejectEmptyResponse {
                authenticateBasicAsync(
                  realm = "secure",
                  Authenticator.UserAuthenticatorAsync
                ) { user =>
                  if (user.email == email)
                    onSuccess(getUser(email)) { response =>
                      complete(response.maybeUser)
                    }
                  else {
                    complete(
                      HttpResponse(
                        StatusCodes.Forbidden,
                        entity = "No Access To That Profile"
                      )
                    )
                  }

                }
              }
              // #retrieve-user-info
            },
            delete {
              // #users-delete-logic
              authenticateBasicAsync(
                realm = "secure",
                Authenticator.UserAuthenticatorAsync
              ) { user =>
                if (user.email == email)
                  onSuccess(deleteUser(email)) { performed =>
                    complete((StatusCodes.OK, performed))
                  }
                else
                  complete(
                    HttpResponse(
                      StatusCodes.Forbidden,
                      entity = "No Access To That Profile"
                    )
                  )
              }
              // #users-delete-logic
            }
          )
        },
        pathPrefix("rating")(userRatingRoutes),
        pathPrefix("review")(userReviewRoutes)
      )
      // #users-get-delete

    }
  // #all-routes
}
