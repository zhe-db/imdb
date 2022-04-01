package edu.duke.imdb.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import _root_.edu.duke.imdb.models.entity._
import _root_.edu.duke.imdb.models.database._

object MovieCrewTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with MovieCrewTableTrait

trait MovieCrewTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}

  protected val crewTable = CrewTable
  protected val movieDetailTable = MovieDetailTable
  import crewTable.CrewRows
  import movieDetailTable.MovieDetailRows

  lazy val schema: profile.SchemaDescription = MovieCrewRows.schema
  def ddl = schema

  implicit def GetResultMoviecrewRow(implicit
      e0: GR[Int],
      e1: GR[Option[String]],
      e2: GR[Option[Int]]
  ): GR[MovieCrew] = GR { prs =>
    import prs._
    MovieCrew.tupled(
      (
        <<[Int],
        <<[Int],
        <<?[String],
        <<[Int],
        <<?[String],
        <<?[String],
        <<?[String],
        <<?[String],
        <<?[Int]
      )
    )
  }

  /** Table description of table moviecrew. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Moviecrew(_tableTag: Tag)
      extends profile.api.Table[MovieCrew](_tableTag, "moviecrew") {
    def * = (
      movieId,
      crewId,
      types,
      castId,
      character,
      job,
      department,
      creditId,
      ordering
    ) <> (MovieCrew.tupled, MovieCrew.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(movieId),
        Rep.Some(crewId),
        types,
        Rep.Some(castId),
        character,
        job,
        department,
        creditId,
        ordering
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ =>
          MovieCrew.tupled((_1.get, _2.get, _3, _4.get, _5, _6, _7, _8, _9))
        )
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column movie_id SqlType(int4) */
    val movieId: Rep[Int] = column[Int]("movie_id")

    /** Database column crew_id SqlType(int4) */
    val crewId: Rep[Int] = column[Int]("crew_id")

    /** Database column types SqlType(text), Default(None) */
    val types: Rep[Option[String]] =
      column[Option[String]]("types", O.Default(None))

    /** Database column cast_id SqlType(int4) */
    val castId: Rep[Int] = column[Int]("cast_id")

    /** Database column character SqlType(varchar), Length(250,true),
      * Default(None)
      */
    val character: Rep[Option[String]] = column[Option[String]](
      "character",
      O.Length(250, varying = true),
      O.Default(None)
    )

    /** Database column job SqlType(varchar), Length(250,true), Default(None) */
    val job: Rep[Option[String]] = column[Option[String]](
      "job",
      O.Length(250, varying = true),
      O.Default(None)
    )

    /** Database column department SqlType(varchar), Length(250,true),
      * Default(None)
      */
    val department: Rep[Option[String]] = column[Option[String]](
      "department",
      O.Length(250, varying = true),
      O.Default(None)
    )

    /** Database column credit_id SqlType(varchar), Length(250,true),
      * Default(None)
      */
    val creditId: Rep[Option[String]] = column[Option[String]](
      "credit_id",
      O.Length(250, varying = true),
      O.Default(None)
    )

    /** Database column ordering SqlType(int4), Default(None) */
    val ordering: Rep[Option[Int]] =
      column[Option[Int]]("ordering", O.Default(None))

    /** Primary key of Moviecrew (database name moviecrew_pkey) */
    val pk = primaryKey("moviecrew_pkey", (movieId, crewId))

    /** Foreign key referencing Crewdetail (database name
      * moviecrew_crew_id_fkey)
      */
    lazy val crewdetailFk =
      foreignKey("moviecrew_crew_id_fkey", crewId, CrewRows)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction
      )

    /** Foreign key referencing Moviedetail (database name
      * moviecrew_movie_id_fkey)
      */
    lazy val moviedetailFk =
      foreignKey("moviecrew_movie_id_fkey", movieId, MovieDetailRows)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.Cascade
      )
  }

  /** Collection-like TableQuery object for table Moviecrew */
  lazy val MovieCrewRows = new TableQuery(tag => new Moviecrew(tag))
}
