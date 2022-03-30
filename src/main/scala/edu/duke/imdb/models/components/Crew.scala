package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.entity.Crew
import _root_.edu.duke.imdb.models.database.CrewTable

trait CrewRepositoryComponent {
  def add(crew: Crew): Future[Int]
  def delete(crewId: Int): Future[Int]
  def get(crewId: Int): Future[Option[Crew]]
  def get(): Future[Seq[Crew]]
}

class CrewRepository(db: Database) extends CrewRepositoryComponent {
  protected val table = CrewTable
  import table.profile.api._
  import table.CrewRows

  override def add(crew: Crew): Future[Int] = db.run {
    CrewRows += crew
  }

  override def delete(crewId: Int): Future[Int] = db.run {
    CrewRows.filter(_.id === crewId).delete
  }

  override def get(crewId: Int): Future[Option[Crew]] = db.run {
    CrewRows.filter(_.id === crewId).result.headOption
  }

  override def get(): Future[Seq[Crew]] = db.run {
    CrewRows.result
  }
}
