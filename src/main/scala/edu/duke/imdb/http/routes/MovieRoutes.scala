package edu.duke.imdb.http.routes

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
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import edu.duke.imdb.models.entity._
import edu.duke.imdb.http.services._
import edu.duke.imdb.http.services.MovieRegistry._
import edu.duke.imdb.http.services.Authenticator

class MovieRoutes(movieRegistry: ActorRef[MovieRegistry.Command])(implicit
    val system: ActorSystem[_]
) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(
    system.settings.config.getDuration("my-app.routes.ask-timeout")
  )

  def addMovie(movie: MovieDetailRow): Future[AddMovieResponse] =
    movieRegistry.ask(AddMovie(movie, _))

  def getCompleteMovie(movieId: Int): Future[CompleteMovieResponse] =
    movieRegistry.ask(GetCompleteMovie(movieId, _))

  def likeMovie(
      userFavMovie: APIUserFavouriteMovie
  ): Future[LikeMovieResponse] =
    movieRegistry.ask(LikeMovie(userFavMovie, _))

  def rateMovie(userRating: APIUserRating): Future[RateMovieResponse] =
    movieRegistry.ask(RateMovie(userRating, _))

  def reviewMovie(userReview: APIUserReview): Future[ReviewMovieResponse] =
    movieRegistry.ask(ReviewMovie(userReview, _))

  val movieRoutes: Route =
    pathPrefix("movies") {
      concat(
        pathEnd {
          post {
            entity(as[MovieDetailRow]) { movie =>
              onSuccess(addMovie(movie)) { response =>
                complete((StatusCodes.Created, response.maybeMovie))
              }
            }
          }
        },
        path("like") {
          pathEnd {
            concat {
              post {
                entity(as[APIUserFavouriteMovie]) { userFavMovie =>
                  onSuccess(likeMovie(userFavMovie)) { response =>
                    complete((StatusCodes.OK, response.result))
                  }
                }
              }
            }
          }
        },
        path("rate") {
          pathEnd {
            concat {
              post {
                entity(as[APIUserRating]) { userRateMovie =>
                  onSuccess(rateMovie(userRateMovie)) { response =>
                    complete((StatusCodes.OK, response.result))
                  }
                }
              }
            }
          }
        },
        path("review") {
          pathEnd {
            concat {
              post {
                entity(as[APIUserReview]) { userReview =>
                  onSuccess(reviewMovie(userReview)) { response =>
                    complete((StatusCodes.OK, response.result))
                  }
                }
              }
            }
          }
        },
        path(Segment) { movieId =>
          concat(
            get {
              onSuccess(getCompleteMovie(movieId.toInt)) { response =>
                complete(response.maybeMovie)
              }
            }
          )
        }
      )
    }
}
