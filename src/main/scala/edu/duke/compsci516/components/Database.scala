package edu.duke.compsci516.components

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile

trait DatabaseComponent {
  val db: Database = Database.forConfig("database.postgre")
  val profile: PostgresProfile = new PostgresProfile {}
}
