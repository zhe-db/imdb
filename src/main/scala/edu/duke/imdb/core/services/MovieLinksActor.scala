package edu.duke.imdb.core.services

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.sql.Timestamp
import java.time.Instant
import _root_.edu.duke.imdb.components.ConfigComponent
import _root_.edu.duke.imdb.distributedCache.MovieLinksCache

object MovieLinksActor extends ConfigComponent {
  sealed trait Command
  val movieLinksReplicatedStore = MovieLinksCache
  final case class CreateMovieLinks(replyTo: ActorRef[Boolean]) extends Command
  final case class ReadMovieLinks(
      movieIds: Seq[Int],
      replyTo: ActorRef[MovieLinksResponse]
  ) extends Command
  final case class MovieLinksResponse(movieLinks: Seq[Int])
}
