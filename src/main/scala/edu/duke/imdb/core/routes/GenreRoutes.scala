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
import _root_.edu.duke.imdb.core.services.GenreRegistry._
import _root_.edu.duke.imdb.core.services.Authenticator

class GenreRoutes(genreRegistry: ActorRef[GenreRegistry.Command])(implicit
    val system: ActorSystem[_]
) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  // #import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(
    system.settings.config.getDuration("my-app.routes.ask-timeout")
  )

  def getGenres(): Future[Genres] =
    genreRegistry.ask(GetGenres)
  def getGenre(genreId: Int): Future[GetGenreResopnse] =
    genreRegistry.ask(GetGenre(genreId, _))
  def addGenre(genre: Genre): Future[AddGenreResponse] =
    genreRegistry.ask(AddGenre(genre, _))
  def deleteGenre(genreId: Int): Future[ActionPerformed] =
    genreRegistry.ask(DeleteGenre(genreId, _))
  def updateGenre(genre: Genre): Future[ActionPerformed] =
    genreRegistry.ask(UpdateGenre(genre, _))
  def getMoviesByGenre(
      genreId: Int,
      sortKey: String,
      limit: Int,
      page: Int
  ): Future[GetMoviesByGenreResponse] =
    genreRegistry.ask(GetMoviesByGenre(genreId, sortKey, limit, page, _))

  val genreRoutes: Route =
    pathPrefix("genres") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getGenres())
            },
            post {
              entity(as[Genre]) { genre =>
                onSuccess(addGenre(genre)) { response =>
                  complete((StatusCodes.Created, response.maybeGenre))
                }
              }
            },
            put {
              entity(as[Genre]) { genre =>
                onSuccess(updateGenre(genre)) { performed =>
                  complete((StatusCodes.OK, performed))
                }
              }
            }
          )
        },
        path("movies") {
          concat(
            pathEnd {
              concat {
                get {
                  parameters(
                    "genre".as[Int],
                    "sortKey".as[Option[String]],
                    "page".as[Int],
                    "limit".as[Int]
                  ) { (genre, sortKey, page, limit) =>
                    onSuccess(
                      getMoviesByGenre(
                        genre,
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
            }
          )
        },
        pathPrefix("detail") {
          path(Segment) { genreId =>
            pathEnd {
              concat(
                get {
                  onSuccess(getGenre(genreId.toInt)) { response =>
                    complete((StatusCodes.OK, response.maybeGenre))
                  }
                },
                delete {
                  authenticateBasicAsync(
                    realm = "secure",
                    Authenticator.UserAuthenticatorAsync
                  ) { user =>
                    onSuccess(deleteGenre(genreId.toInt)) { performed =>
                      complete((StatusCodes.OK, performed))
                    }
                  }
                }
              )
            }
          }
        }
      )
    }
}
