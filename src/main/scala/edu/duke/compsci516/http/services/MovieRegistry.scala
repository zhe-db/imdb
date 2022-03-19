package edu.duke.compsci516.http.services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.sql.Timestamp
import java.time.Instant

import edu.duke.compsci516.models.entity.Movie
import edu.duke.compsci516.components.DatabaseComponent

object MovieRegistry extends DatabaseComponent {
  private val movieRpo = None

  sealed trait Command
  final case class GetMovie(
      movidId: Integer,
      replyTo: ActorRef[GetMovieResponse]
  ) extends Command

  final case class GetMovieResponse(maybeMovie: Option[Movie])
}
