package edu.duke.imdb.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import _root_.edu.duke.imdb.models.entity._

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
      e3: GR[String],
      e4: GR[Option[Int]]
  ): GR[Crew] = GR { prs =>
    import prs._
    Crew.tupled(
      (
        <<[Int],
        <<?[java.sql.Date],
        <<?[String],
        <<[String],
        <<?[Int],
        <<?[String],
        <<?[String],
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
      profilePath,
      homepage,
      imdbId
    ) <> (Crew.tupled, Crew.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(id),
        birthday,
        knowForDepartment,
        Rep.Some(name),
        gender,
        biography,
        placeOfBirth,
        profilePath,
        homepage,
        imdbId
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ =>
          Crew.tupled(
            (_1.get, _2, _3, _4.get, _5, _6, _7, _8, _9, _10)
          )
        )
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

    /** Database column gender SqlType(int4), Default(None) */
    val gender: Rep[Option[Int]] =
      column[Option[Int]]("gender", O.Default(None))

    /** Database column biography SqlType(text), Default(None) */
    val biography: Rep[Option[String]] =
      column[Option[String]]("biography", O.Default(None))

    /** Database column place_of_birth SqlType(text), Default(None) */
    val placeOfBirth: Rep[Option[String]] =
      column[Option[String]]("place_of_birth", O.Default(None))

    /** Database column profile_path SqlType(text), Default(None) */
    val profilePath: Rep[Option[String]] =
      column[Option[String]]("profile_path", O.Default(None))

    /** Database column homepage SqlType(text), Default(None) */
    val homepage: Rep[Option[String]] =
      column[Option[String]]("homepage", O.Default(None))

    /** Database column imdb_id SqlType(int4), Default(None) */
    val imdbId: Rep[Option[String]] =
      column[Option[String]]("imdb_id", O.Default(None))
  }

  /** Collection-like TableQuery object for table Crewdetail */
  lazy val CrewRows = new TableQuery(tag => new Crewdetail(tag))

}
