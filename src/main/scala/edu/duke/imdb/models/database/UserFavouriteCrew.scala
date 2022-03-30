package edu.duke.imdb.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.imdb.models.entity.{UserFavouriteCrew}
import edu.duke.imdb.models.database.{UserTable, CrewTable}

object UserFavouriteCrewTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with UserFavouriteCrewTableTrait

trait UserFavouriteCrewTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}

  protected val userTable = UserTable
  protected val crewTable = CrewTable
  import userTable.Users
  import crewTable.CrewRows

  lazy val schema: profile.SchemaDescription = UserFavouriteCrewRows.schema
  def ddl = schema

  implicit def GetResultUserfavactorRow(implicit
      e0: GR[java.util.UUID],
      e1: GR[Option[Int]]
  ): GR[UserFavouriteCrew] = GR { prs =>
    import prs._
    UserFavouriteCrew.tupled((<<[java.util.UUID], <<[java.util.UUID], <<?[Int]))
  }

  /** Table description of table userfavactor. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Userfavactor(_tableTag: Tag)
      extends profile.api.Table[UserFavouriteCrew](_tableTag, "userfavactor") {
    def * =
      (
        id,
        userId,
        crewId
      ) <> (UserFavouriteCrew.tupled, UserFavouriteCrew.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(userId), crewId)).shaped.<>(
      { r =>
        import r._; _1.map(_ => UserFavouriteCrew.tupled((_1.get, _2.get, _3)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(uuid), PrimaryKey */
    val id: Rep[java.util.UUID] = column[java.util.UUID]("id", O.PrimaryKey)

    /** Database column user_id SqlType(uuid) */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id")

    /** Database column actor_id SqlType(int4), Default(None) */
    val crewId: Rep[Option[Int]] =
      column[Option[Int]]("actor_id", O.Default(None))

    /** Foreign key referencing Crewdetail (database name
      * userfavactor_actor_id_fkey)
      */
    lazy val crewdetailFk =
      foreignKey("userfavactor_actor_id_fkey", crewId, CrewRows)(
        r => Rep.Some(r.id),
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.Cascade
      )

    /** Foreign key referencing Users (database name userfavactor_user_id_fkey)
      */
    lazy val usersFk = foreignKey("userfavactor_user_id_fkey", userId, Users)(
      r => r.userId,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.Cascade
    )

    /** Uniqueness Index over (userId,actorId) (database name per_user_actor) */
    val index1 = index("per_user_actor", (userId, crewId), unique = true)
  }

  /** Collection-like TableQuery object for table Userfavactor */
  lazy val UserFavouriteCrewRows = new TableQuery(tag => new Userfavactor(tag))
}
