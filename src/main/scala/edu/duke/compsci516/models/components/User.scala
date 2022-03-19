package edu.duke.compsci516.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import edu.duke.compsci516.models.entity.User
import edu.duke.compsci516.models.database.UserTable

trait UserRepositoryComponent {
  def add(account: User): Future[java.util.UUID]
  def updateUsername(email: String, username: String): Future[Int]
  def deleteBy(account_email: String): Future[Int]
  def deleteBy(account_uuid: java.util.UUID): Future[Int]
  def get(account_uuid: java.util.UUID): Future[Option[User]]
  def get(account_email: String): Future[Option[User]]
  def getUsers(): Future[Seq[User]]
  def updateLastLogin(
      userId: java.util.UUID,
      lastLogin: java.sql.Timestamp
  ): Future[Int]
}

class UserRepository(db: Database) extends UserRepositoryComponent {
  protected val table = UserTable
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

  import java.util.UUID.randomUUID
  import java.sql.Timestamp
  import java.time.LocalDateTime
  import java.time.Instant
  def test() {
    val uuid = randomUUID
    println(uuid)
    val test_user1 =
      new User(
        uuid,
        "test1",
        "test1@gmail.com",
        "123456",
        Timestamp.from(Instant.now()),
        None
      )
    add(test_user1)
  }
}
