package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.models.database._
import java.util.UUID
import scala.concurrent.ExecutionContext

trait UserRepositoryComponent {
  def add(account: User): Future[java.util.UUID]
  def updateUsername(email: String, username: String): Future[Int]
  def deleteBy(account_email: String): Future[Int]
  def deleteBy(account_uuid: java.util.UUID): Future[Int]
  def get(account_uuid: java.util.UUID): Future[Option[User]]
  def get(account_email: String): Future[Option[User]]
  def getUserInfo(userId: java.util.UUID): Future[Option[User]]
  def getUsers(): Future[Seq[User]]
  def updateLastLogin(
      userId: java.util.UUID,
      lastLogin: java.sql.Timestamp
  ): Future[Int]
}

class UserRepository(db: Database) extends UserRepositoryComponent {
  protected val table = UserTable
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  import table.profile.api._
  import table.Users

  override def add(user: User): Future[java.util.UUID] = db.run {
    (Users returning Users.map(_.userId)) += user
  }

  override def updateUsername(email: String, username: String): Future[Int] =
    db.run {
      Users
        .filter(_.email === email)
        .map(_.name)
        .update(username)
    }

  override def updateLastLogin(
      userId: java.util.UUID,
      lastLogin: java.sql.Timestamp
  ): Future[Int] = db.run {
    Users.filter(_.userId === userId).map(_.lastLogin).update(Some(lastLogin))
  }

  override def deleteBy(email: String): Future[Int] =
    db.run {
      Users.filter(_.email === email).delete
    }

  override def deleteBy(uuid: java.util.UUID): Future[Int] =
    db.run {
      Users.filter(_.userId === uuid).delete
    }

  override def get(email: String): Future[Option[User]] = db.run {
    Users.filter(_.email === email).result.headOption
  }

  override def get(uuid: java.util.UUID): Future[Option[User]] =
    db.run {
      Users.filter(_.userId === uuid).result.headOption
    }
  override def getUsers(): Future[Seq[User]] = db.run {
    Users.result
  }

  override def getUserInfo(userId: UUID): Future[Option[User]] = db.run {
    Users
      .filter(_.userId === userId)
      .result
      .headOption
  }

}
