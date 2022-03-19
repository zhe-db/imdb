package edu.duke.compsci516.http.routes

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

import edu.duke.compsci516.models.entity.{Genre, Genres}
import edu.duke.compsci516.http.services._
import edu.duke.compsci516.http.services.GenreRegistry._
import edu.duke.compsci516.http.services.Authenticator

class GenreRoutes(genreRegistry: ActorRef[GenreRegistry.Command])(implicit
    val system: ActorSystem[_]
) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

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

  val genreRoutes: Route =
    pathPrefix("genres") {
      concat(
        pathEnd {
          concat(
            get {
              authenticateBasicAsync(
                realm = "secure",
                Authenticator.UserAuthenticatorAsync
              ) { user =>
                complete(getGenres())
              }
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
        path(Segment) { genreId =>
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
      )
    }
}
