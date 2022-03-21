package edu.duke.compsci516.models.entity

import java.util.UUID.randomUUID
import java.time.Instant
import java.sql.Timestamp

import edu.duke.compsci516.utils.Encryption

/** Entity class storing rows of table Users
  * @param userId
  *   Database column user_id SqlType(uuid), PrimaryKey
  * @param name
  *   Database column name SqlType(varchar), Length(250,true)
  * @param email
  *   Database column email SqlType(citext)
  * @param password
  *   Database column password SqlType(text)
  * @param createdOn
  *   Database column created_on SqlType(timestamp)
  * @param lastLogin
  *   Database column last_login SqlType(timestamp), Default(None)
  */

case class User(
    userId: java.util.UUID,
    name: String,
    email: String,
    password: String,
    createdOn: java.sql.Timestamp,
    lastLogin: Option[java.sql.Timestamp] = None
)

case class Users(users: Seq[User])

case class APIUser(
    name: String,
    email: String,
    password: String
) {
  def toUser(): User = {
    val uuid: java.util.UUID = randomUUID
    val createdOn: java.sql.Timestamp = Timestamp.from(Instant.now())
    val lastLogin: java.sql.Timestamp = Timestamp.from(Instant.now())
    val encrypted_password: String = Encryption.encrypt(password)
    return User(
      uuid,
      name,
      email,
      encrypted_password,
      createdOn,
      Some(lastLogin)
    )
  }
}

case class UserFavouriteCrew(
    id: java.util.UUID,
    userId: java.util.UUID,
    crewId: Option[Int] = None
)

case class UserFavouriteMovie(
    id: java.util.UUID,
    userId: java.util.UUID,
    movieId: Option[Int] = None
)

/** Entity class storing rows of table Userratings
  * @param id
  *   Database column id SqlType(uuid), PrimaryKey
  * @param userId
  *   Database column user_id SqlType(uuid)
  * @param movieId
  *   Database column movie_id SqlType(int4), Default(None)
  * @param rating
  *   Database column rating SqlType(float8), Default(None)
  */
case class UserRating(
    id: java.util.UUID,
    userId: java.util.UUID,
    movieId: Option[Int] = None,
    rating: Option[Double] = None
)

case class APIUserRating(
    userId: java.util.UUID,
    movieId: Option[Int] = None,
    rating: Option[Double] = None
) {
  def toUserRating(): UserRating = {
    val id: java.util.UUID = randomUUID
    return UserRating(id, userId, movieId, rating)
  }
}

/** Entity class storing rows of table Userreview
  * @param id
  *   Database column id SqlType(uuid), PrimaryKey
  * @param userId
  *   Database column user_id SqlType(uuid)
  * @param movieId
  *   Database column movie_id SqlType(int4), Default(None)
  * @param contents
  *   Database column contents SqlType(text), Default(None)
  */
case class UserReview(
    id: java.util.UUID,
    userId: java.util.UUID,
    movieId: Option[Int] = None,
    contents: Option[String] = None
)

/** Entity class storing rows of table Userview
  * @param id
  *   Database column id SqlType(uuid), PrimaryKey
  * @param userId
  *   Database column user_id SqlType(uuid)
  * @param movieId
  *   Database column movie_id SqlType(int4), Default(None)
  */
case class UserView(
    id: java.util.UUID,
    userId: java.util.UUID,
    movieId: Option[Int] = None
)
