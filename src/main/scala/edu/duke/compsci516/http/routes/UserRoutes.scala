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

import edu.duke.compsci516.models.entity.{User, Users, APIUser}
import edu.duke.compsci516.http.services._
import edu.duke.compsci516.http.services.UserRegistry._
import edu.duke.compsci516.http.services.Authenticator

class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit
    val system: ActorSystem[_]
) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

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

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        //#users-get-delete
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
        //#users-get-delete
        //#users-get-post
        pathPrefix("login") {
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
        path(Segment) { email =>
          concat(
            get {
              //#retrieve-user-info
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
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
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
              //#users-delete-logic
            }
          )
        }
      )
      //#users-get-delete
    }
  //#all-routes
}
