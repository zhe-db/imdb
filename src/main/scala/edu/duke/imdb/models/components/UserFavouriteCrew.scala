package edu.duke.imdb.models.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

import _root_.edu.duke.imdb.models.database.UserFavouriteCrewTable
import _root_.edu.duke.imdb.models.entity.UserFavouriteCrew

trait UserFavouriteCrewRepositoryComponent {
  def add(userFavCrew: UserFavouriteCrew): Future[Int]
  def deleteUser(userId: java.util.UUID): Future[Int]
  def deleteCrew(crewId: Int): Future[Int]
  def getUserFavouriteCrews(userId: java.util.UUID): Future[Seq[Option[Int]]]
  def getUsersByCrew(crewId: Int): Future[Seq[java.util.UUID]]
}

class UserFavouriteCrewRepository(db: Database)
    extends UserFavouriteCrewRepositoryComponent {
  protected val table = UserFavouriteCrewTable
  import table.profile.api._
  import table.UserFavouriteCrewRows

  override def add(userFavCrew: UserFavouriteCrew): Future[Int] = db.run {
    UserFavouriteCrewRows += userFavCrew
  }

  override def deleteUser(userId: java.util.UUID): Future[Int] = db.run {
    UserFavouriteCrewRows.filter(_.userId === userId).delete
  }

  override def deleteCrew(crewId: Int): Future[Int] = db.run {
    UserFavouriteCrewRows.filter(_.crewId === crewId).delete
  }

  override def getUserFavouriteCrews(
      userId: java.util.UUID
  ): Future[Seq[Option[Int]]] =
    db.run {
      UserFavouriteCrewRows.filter(_.userId === userId).map(_.crewId).result
    }

  override def getUsersByCrew(crewId: Int): Future[Seq[java.util.UUID]] =
    db.run {
      UserFavouriteCrewRows.filter(_.crewId === crewId).map(_.userId).result
    }
}
