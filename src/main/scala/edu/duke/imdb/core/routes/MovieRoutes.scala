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
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.core.services._
import _root_.edu.duke.imdb.core.services.MovieRegistry._
import _root_.edu.duke.imdb.core.services.Authenticator

class MovieRoutes(movieRegistry: ActorRef[MovieRegistry.Command])(implicit
    val system: ActorSystem[_]
) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  // #import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(
    system.settings.config.getDuration("my-app.routes.ask-timeout")
  )

  def addMovie(movie: MovieDetailRow): Future[AddMovieResponse] =
    movieRegistry.ask(AddMovie(movie, _))

  def getCompleteMovie(movieId: Int): Future[CompleteMovieResponse] =
    movieRegistry.ask(GetCompleteMovie(movieId, _))

  def getMoviesByRating(
      rating: Double,
      sortKey: String,
      page: Int,
      limit: Int
  ): Future[PaginatedMoviesResponse] =
    movieRegistry.ask(GetMoviesByRating(rating, sortKey, limit, page, _))

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
        pathPrefix("list") {
          concat(
            path("rating") {
              get {
                parameters(
                  "rating".as[Double],
                  "sortKey".as[Option[String]],
                  "page".as[Int],
                  "limit".as[Int]
                ) { (rating, sortKey, page, limit) =>
                  onSuccess(
                    getMoviesByRating(
                      rating,
                      sortKey.getOrElse(""),
                      page,
                      limit
                    )
                  ) { response =>
                    complete((StatusCodes.OK, response.movies))
                  }
                }
              }
            }
          )
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
