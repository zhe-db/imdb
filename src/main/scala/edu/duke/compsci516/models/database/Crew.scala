package edu.duke.compsci516.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.compsci516.models.entity.{Crew}

object CrewTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with CrewTableTrait

trait CrewTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}
  lazy val schema: profile.SchemaDescription = CrewRows.schema
  def ddl = schema

  implicit def GetResultCrewdetailRow(implicit
      e0: GR[Int],
      e1: GR[Option[java.sql.Date]],
      e2: GR[Option[String]],
      e3: GR[String]
  ): GR[Crew] = GR { prs =>
    import prs._
    Crew.tupled(
      (
        <<[Int],
        <<?[java.sql.Date],
        <<?[String],
        <<[String],
        <<[Int],
        <<?[String],
        <<?[String],
        <<?[String]
      )
    )
  }

  /** Table description of table crewdetail. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Crewdetail(_tableTag: Tag)
      extends profile.api.Table[Crew](_tableTag, "crewdetail") {
    def * = (
      id,
      birthday,
      knowForDepartment,
      name,
      gender,
      biography,
      placeOfBirth,
      profilePath
    ) <> (Crew.tupled, Crew.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(id),
        birthday,
        knowForDepartment,
        Rep.Some(name),
        Rep.Some(gender),
        biography,
        placeOfBirth,
        profilePath
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ => Crew.tupled((_1.get, _2, _3, _4.get, _5.get, _6, _7, _8)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)

    /** Database column birthday SqlType(date), Default(None) */
    val birthday: Rep[Option[java.sql.Date]] =
      column[Option[java.sql.Date]]("birthday", O.Default(None))

    /** Database column know_for_department SqlType(text), Default(None) */
    val knowForDepartment: Rep[Option[String]] =
      column[Option[String]]("know_for_department", O.Default(None))

    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")

    /** Database column gender SqlType(int4) */
    val gender: Rep[Int] = column[Int]("gender")

    /** Database column biography SqlType(text), Default(None) */
    val biography: Rep[Option[String]] =
      column[Option[String]]("biography", O.Default(None))

    /** Database column place_of_birth SqlType(text), Default(None) */
    val placeOfBirth: Rep[Option[String]] =
      column[Option[String]]("place_of_birth", O.Default(None))

    /** Database column profile_path SqlType(text), Default(None) */
    val profilePath: Rep[Option[String]] =
      column[Option[String]]("profile_path", O.Default(None))
  }

  /** Collection-like TableQuery object for table Crewdetail */
  lazy val CrewRows = new TableQuery(tag => new Crewdetail(tag))

}
