package edu.duke.imdb.models.database

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.jdbc.PostgresProfile.api._

import _root_.edu.duke.imdb.models.entity._

object GenreTable
    extends {
      val profile = slick.jdbc.PostgresProfile
    }
    with GenreTableTrait

trait GenreTableTrait {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  import slick.jdbc.{GetResult => GR}
  lazy val schema: profile.SchemaDescription = GenreRows.schema
  def ddl = schema

  implicit def GetResultGenreRow(implicit
      e0: GR[Int],
      e1: GR[String]
  ): GR[Genre] = GR { prs =>
    import prs._
    Genre.tupled((<<[Int], <<[String]))
  }

  class GenreR(_tableTag: Tag)
      extends profile.api.Table[Genre](_tableTag, "genre") {
    def * = (id, name) <> (Genre.tupled, Genre.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name))).shaped.<>(
      { r => import r._; _1.map(_ => Genre.tupled((_1.get, _2.get))) },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)

    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
  }

  /** Collection-like TableQuery object for table Genre */
  lazy val GenreRows = new TableQuery(tag => new GenreR(tag))
}
