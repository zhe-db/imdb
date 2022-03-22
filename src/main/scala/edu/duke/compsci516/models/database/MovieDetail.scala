package edu.duke.compsci516.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.compsci516.models.entity.{MovieDetailRow}

object MovieDetailTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with MovieDetailTableTrait

trait MovieDetailTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}
  lazy val schema: profile.SchemaDescription = MovieDetailRows.schema
  def ddl = schema

  implicit def GetResultMoviedetailRow(implicit
      e0: GR[Int],
      e1: GR[Boolean],
      e2: GR[Option[String]],
      e3: GR[Option[Int]],
      e4: GR[String],
      e5: GR[Option[Double]],
      e6: GR[Option[java.sql.Date]]
  ): GR[MovieDetailRow] = GR { prs =>
    import prs._
    MovieDetailRow.tupled(
      (
        <<[Int],
        <<[Boolean],
        <<?[String],
        <<?[Int],
        <<[String],
        <<[String],
        <<?[String],
        <<?[Double],
        <<?[String],
        <<?[java.sql.Date],
        <<?[Int],
        <<?[Int],
        <<?[Double],
        <<?[String],
        <<?[Int],
        <<?[String]
      )
    )
  }

  /** Table description of table moviedetail. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Moviedetail(_tableTag: Tag)
      extends profile.api.Table[MovieDetailRow](_tableTag, "moviedetail") {
    def * = (
      id,
      adult,
      backdropPath,
      budget,
      imdbId,
      title,
      overview,
      popularity,
      posterPath,
      releaseDate,
      runtime,
      revenue,
      voteAverage,
      homepage,
      voteCount,
      tagline
    ) <> (MovieDetailRow.tupled, MovieDetailRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (
      (
        Rep.Some(id),
        Rep.Some(adult),
        backdropPath,
        budget,
        Rep.Some(imdbId),
        Rep.Some(title),
        overview,
        popularity,
        posterPath,
        releaseDate,
        runtime,
        revenue,
        voteAverage,
        homepage,
        voteCount,
        tagline
      )
    ).shaped.<>(
      { r =>
        import r._;
        _1.map(_ =>
          MovieDetailRow.tupled(
            (
              _1.get,
              _2.get,
              _3,
              _4,
              _5.get,
              _6.get,
              _7,
              _8,
              _9,
              _10,
              _11,
              _12,
              _13,
              _14,
              _15,
              _16
            )
          )
        )
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)

    /** Database column adult SqlType(bool) */
    val adult: Rep[Boolean] = column[Boolean]("adult")

    /** Database column backdrop_path SqlType(text), Default(None) */
    val backdropPath: Rep[Option[String]] =
      column[Option[String]]("backdrop_path", O.Default(None))

    /** Database column budget SqlType(int4), Default(None) */
    val budget: Rep[Option[Int]] =
      column[Option[Int]]("budget", O.Default(None))

    /** Database column imdb_id SqlType(text) */
    val imdbId: Rep[String] = column[String]("imdb_id")

    /** Database column title SqlType(text) */
    val title: Rep[String] = column[String]("title")

    /** Database column overview SqlType(text), Default(None) */
    val overview: Rep[Option[String]] =
      column[Option[String]]("overview", O.Default(None))

    /** Database column popularity SqlType(float8), Default(None) */
    val popularity: Rep[Option[Double]] =
      column[Option[Double]]("popularity", O.Default(None))

    /** Database column poster_path SqlType(text), Default(None) */
    val posterPath: Rep[Option[String]] =
      column[Option[String]]("poster_path", O.Default(None))

    /** Database column release_date SqlType(date), Default(None) */
    val releaseDate: Rep[Option[java.sql.Date]] =
      column[Option[java.sql.Date]]("release_date", O.Default(None))

    /** Database column runtime SqlType(int4), Default(None) */
    val runtime: Rep[Option[Int]] =
      column[Option[Int]]("runtime", O.Default(None))

    /** Database column revenue SqlType(int4), Default(None) */
    val revenue: Rep[Option[Int]] =
      column[Option[Int]]("revenue", O.Default(None))

    /** Database column vote_average SqlType(float8), Default(None) */
    val voteAverage: Rep[Option[Double]] =
      column[Option[Double]]("vote_average", O.Default(None))

    /** Database column homepage SqlType(text), Default(None) */
    val homepage: Rep[Option[String]] =
      column[Option[String]]("homepage", O.Default(None))

    /** Database column vote_count SqlType(int4), Default(None) */
    val voteCount: Rep[Option[Int]] =
      column[Option[Int]]("vote_count", O.Default(None))

    /** Database column tagline SqlType(text), Default(None) */
    val tagline: Rep[Option[String]] =
      column[Option[String]]("tagline", O.Default(None))
  }

  /** Collection-like TableQuery object for table Moviedetail */
  lazy val MovieDetailRows = new TableQuery(tag => new Moviedetail(tag))
}
