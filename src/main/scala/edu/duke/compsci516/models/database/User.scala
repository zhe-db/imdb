package edu.duke.compsci516.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.compsci516.models.entity.User

object UserTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with UserTableTrait

/** Slick data model trait for extension, choice of backend or usage in the cake
  * pattern. (Make sure to initialize this late.)
  */
trait UserTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Users.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** GetResult implicit for fetching UsersRow objects using plain SQL queries
    */
  implicit def GetResultUsersRow(implicit
      e0: GR[java.util.UUID],
      e1: GR[String],
      e2: GR[java.sql.Timestamp],
      e3: GR[Option[java.sql.Timestamp]]
  ): GR[User] = GR { prs =>
    import prs._
    User.tupled(
      (
        <<[java.util.UUID],
        <<[String],
        <<[String],
        <<[String],
        <<[java.sql.Timestamp],
        <<?[java.sql.Timestamp]
      )
    )
  }

  /** Table description of table users. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Users(_tableTag: Tag)
      extends profile.api.Table[User](_tableTag, "users") {
    def * = (
      userId,
      name,
      email,
      password,
      createdOn,
      lastLogin
    ) <> (User.tupled, User.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(userId),
        Rep.Some(name),
        Rep.Some(email),
        Rep.Some(password),
        Rep.Some(createdOn),
        lastLogin
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ => User.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column user_id SqlType(uuid), PrimaryKey */
    val userId: Rep[java.util.UUID] =
      column[java.util.UUID]("user_id", O.PrimaryKey)

    /** Database column name SqlType(varchar), Length(250,true) */
    val name: Rep[String] =
      column[String]("name", O.Length(250, varying = true))

    /** Database column email SqlType(citext) */
    val email: Rep[String] = column[String]("email")

    /** Database column password SqlType(text) */
    val password: Rep[String] = column[String]("password")

    /** Database column created_on SqlType(timestamp) */
    val createdOn: Rep[java.sql.Timestamp] =
      column[java.sql.Timestamp]("created_on")

    /** Database column last_login SqlType(timestamp), Default(None) */
    val lastLogin: Rep[Option[java.sql.Timestamp]] =
      column[Option[java.sql.Timestamp]]("last_login", O.Default(None))

    /** Uniqueness Index over (email) (database name users_email_key) */
    val index1 = index("users_email_key", email, unique = true)
  }

  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))
}
