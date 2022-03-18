package edu.duke.compsci516.http.services

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials

import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import edu.duke.compsci516.models.components.UserRepository
import edu.duke.compsci516.components.DatabaseComponent
import edu.duke.compsci516.utils.Encryption
import edu.duke.compsci516.models.entity.User

object Authenticator extends DatabaseComponent {
  private val userRepo = new UserRepository(this.db)

  def UserAuthenticatorAsync(
      credentials: Credentials
  ): Future[Option[User]] = {
    credentials match {
      case p @ Credentials.Provided(id) =>
        userRepo.get(id).map {
          case Some(user) if p.verify(user.password, Encryption.encrypt) =>
            Some(user)
          case _ => None
        }
      case _ => Future.successful(None)
    }
  }
}
