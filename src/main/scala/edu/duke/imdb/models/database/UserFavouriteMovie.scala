package edu.duke.imdb.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.models.database._

object UserFavouriteMovieTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with UserFavouriteMovieTableTrait

trait UserFavouriteMovieTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}

  protected val userTable = UserTable
  protected val movieDetailTable = MovieDetailTable
  import userTable.Users
  import movieDetailTable.MovieDetailRows

  lazy val schema: profile.SchemaDescription = UserFavouriteMovieRows.schema
  def ddl = schema

  implicit def GetResultUserfavmovieRow(implicit
      e0: GR[java.util.UUID],
      e1: GR[Option[Int]]
  ): GR[UserFavouriteMovie] = GR { prs =>
    import prs._
    UserFavouriteMovie.tupled(
      (<<[java.util.UUID], <<[java.util.UUID], <<?[Int])
    )
  }

  /** Table description of table userfavmovie. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Userfavmovie(_tableTag: Tag)
      extends profile.api.Table[UserFavouriteMovie](_tableTag, "userfavmovie") {
    def * =
      (
        id,
        userId,
        movieId
      ) <> (UserFavouriteMovie.tupled, UserFavouriteMovie.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(userId), movieId)).shaped.<>(
      { r =>
        import r._; _1.map(_ => UserFavouriteMovie.tupled((_1.get, _2.get, _3)))
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
      * userfavmovie_movie_id_fkey)
      */
    lazy val moviedetailFk =
      foreignKey("userfavmovie_movie_id_fkey", movieId, MovieDetailRows)(
        r => Rep.Some(r.id),
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.Cascade
      )

    /** Foreign key referencing Users (database name userfavmovie_user_id_fkey)
      */
    lazy val usersFk = foreignKey("userfavmovie_user_id_fkey", userId, Users)(
      r => r.userId,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )

    /** Uniqueness Index over (userId,movieId) (database name per_user_movie) */
    val index1 = index("per_user_movie", (userId, movieId), unique = true)
  }

  /** Collection-like TableQuery object for table Userfavmovie */
  lazy val UserFavouriteMovieRows = new TableQuery(tag => new Userfavmovie(tag))
}
