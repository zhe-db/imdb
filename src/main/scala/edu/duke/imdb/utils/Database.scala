package edu.duke.imdb.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile

import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._

trait DatabaseComponent {
  val db: Database = Database.forConfig("database.postgre")
  val profile: PostgresProfile = new PostgresProfile {}
}
