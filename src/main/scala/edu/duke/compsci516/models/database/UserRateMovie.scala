package edu.duke.compsci516.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.compsci516.models.entity.{UserRating}
import edu.duke.compsci516.models.database.{UserTable, MovieDetailTable}

object UserRateMovieTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with UserRateMovieTableTrait

trait UserRateMovieTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}

  protected val userTable = UserTable
  protected val movieDetailTable = MovieDetailTable
  import userTable.Users
  import movieDetailTable.MovieDetailRows

  lazy val schema: profile.SchemaDescription = UserRateMovieRows.schema
  def ddl = schema
  implicit def GetResultUserratingsRow(implicit
      e0: GR[java.util.UUID],
      e1: GR[Option[Int]],
      e2: GR[Option[Double]]
  ): GR[UserRating] = GR { prs =>
    import prs._
    UserRating.tupled(
      (<<[java.util.UUID], <<[java.util.UUID], <<?[Int], <<?[Double])
    )
  }

  /** Table description of table userratings. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Userratings(_tableTag: Tag)
      extends profile.api.Table[UserRating](_tableTag, "userratings") {
    def * =
      (id, userId, movieId, rating) <> (UserRating.tupled, UserRating.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(userId), movieId, rating)).shaped.<>(
      { r =>
        import r._; _1.map(_ => UserRating.tupled((_1.get, _2.get, _3, _4)))
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

    /** Database column rating SqlType(float8), Default(None) */
    val rating: Rep[Option[Double]] =
      column[Option[Double]]("rating", O.Default(None))

    /** Foreign key referencing Moviedetail (database name
      * userratings_movie_id_fkey)
      */
    lazy val moviedetailFk =
      foreignKey("userratings_movie_id_fkey", movieId, MovieDetailRows)(
        r => Rep.Some(r.id),
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.Cascade
      )

    /** Foreign key referencing Users (database name userratings_user_id_fkey)
      */
    lazy val usersFk = foreignKey("userratings_user_id_fkey", userId, Users)(
      r => r.userId,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )

    /** Uniqueness Index over (userId,movieId) (database name per_user_rating)
      */
    val index1 = index("per_user_rating", (userId, movieId), unique = true)
  }

  /** Collection-like TableQuery object for table Userratings */
  lazy val UserRateMovieRows = new TableQuery(tag => new Userratings(tag))
}
