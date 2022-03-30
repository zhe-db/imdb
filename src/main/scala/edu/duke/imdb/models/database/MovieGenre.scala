package edu.duke.imdb.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import edu.duke.imdb.models.entity.{MovieGenre, Genre}
import edu.duke.imdb.models.database.{GenreTable, MovieDetailTable}

object MovieGenreTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with MovieGenreTableTrait

trait MovieGenreTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}

  protected val genreTable = GenreTable
  protected val movieDetailTable = MovieDetailTable
  import genreTable.GenreRows
  import movieDetailTable.MovieDetailRows

  lazy val schema: profile.SchemaDescription = MovieGenreRows.schema
  def ddl = schema

  implicit def GetResultMoviegenreRow(implicit e0: GR[Int]): GR[MovieGenre] =
    GR { prs =>
      import prs._
      MovieGenre.tupled((<<[Int], <<[Int]))
    }

  /** Table description of table moviegenre. Objects of this class serve as
    * prototypes for rows in queries.
    */
  class Moviegenre(_tableTag: Tag)
      extends profile.api.Table[MovieGenre](_tableTag, "moviegenre") {
    def * = (movieId, genreId) <> (MovieGenre.tupled, MovieGenre.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(movieId), Rep.Some(genreId))).shaped.<>(
      { r => import r._; _1.map(_ => MovieGenre.tupled((_1.get, _2.get))) },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column movie_id SqlType(int4) */
    val movieId: Rep[Int] = column[Int]("movie_id")

    /** Database column genre_id SqlType(int4) */
    val genreId: Rep[Int] = column[Int]("genre_id")

    /** Primary key of Moviegenre (database name moviegenre_pkey) */
    val pk = primaryKey("moviegenre_pkey", (movieId, genreId))

    /** Foreign key referencing Genre (database name moviegenre_genre_id_fkey)
      */
    lazy val genreFk =
      foreignKey("moviegenre_genre_id_fkey", genreId, GenreRows)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction
      )

    /** Foreign key referencing Moviedetail (database name
      * moviegenre_movie_id_fkey)
      */
    lazy val moviedetailFk =
      foreignKey("moviegenre_movie_id_fkey", movieId, MovieDetailRows)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.Cascade
      )
  }

  /** Collection-like TableQuery object for table Moviegenre */
  lazy val MovieGenreRows = new TableQuery(tag => new Moviegenre(tag))
}
