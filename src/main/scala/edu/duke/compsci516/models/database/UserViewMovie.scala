package edu.duke.compsci516.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.compsci516.models.entity.{UserView}
import edu.duke.compsci516.models.database.{UserTable, MovieDetailTable}

object UserViewMovieTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with UserViewMovieTableTrait

trait UserViewMovieTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}

  protected val userTable = UserTable
  protected val movieDetailTable = MovieDetailTable
  import userTable.Users
  import movieDetailTable.MovieDetailRows

  lazy val schema: profile.SchemaDescription = UserViewMovieRows.schema
  def ddl = schema

  implicit def GetResultUserviewRow(implicit
      e0: GR[java.util.UUID],
      e1: GR[Option[Int]]
  ): GR[UserView] = GR { prs =>
    import prs._
    UserView.tupled((<<[java.util.UUID], <<[java.util.UUID], <<?[Int]))
  }

  /** Table description of table userview. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Userview(_tableTag: Tag)
      extends profile.api.Table[UserView](_tableTag, "userview") {
    def * = (id, userId, movieId) <> (UserView.tupled, UserView.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(userId), movieId)).shaped.<>(
      { r =>
        import r._; _1.map(_ => UserView.tupled((_1.get, _2.get, _3)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)

    /** Database column user_id SqlType(uuid) */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id")

    /** Database column movie_id SqlType(int4), Default(None) */
    val movieId: Rep[Option[Int]] =
      column[Option[Int]]("movie_id", O.Default(None))

    /** Foreign key referencing Moviedetail (database name
      * userview_movie_id_fkey)
      */
    lazy val moviedetailFk =
      foreignKey("userview_movie_id_fkey", movieId, MovieDetailRows)(
        r => Rep.Some(r.id),
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.Cascade
      )

    /** Foreign key referencing Users (database name userview_user_id_fkey) */
    lazy val usersFk = foreignKey("userview_user_id_fkey", userId, Users)(
      r => r.userId,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )

    /** Uniqueness Index over (userId,movieId) (database name per_user_view) */
    val index1 = index("per_user_view", (userId, movieId), unique = true)
  }

  /** Collection-like TableQuery object for table Userview */
  lazy val UserViewMovieRows = new TableQuery(tag => new Userview(tag))
}
