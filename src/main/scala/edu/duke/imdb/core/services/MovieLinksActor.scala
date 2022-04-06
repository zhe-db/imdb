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
import _root_.edu.duke.imdb.data.delta.tables.MovieLensLinksDeltaTable
import _root_.edu.duke.imdb.memStore.ReplicatedCache

object MovieLinksActor extends ConfigComponent {
  sealed trait Command
  val movieLinksReplicatedStore = MovieLinksCache
  import movieLinksReplicatedStore._
  val linksTable = new MovieLensLinksDeltaTable()

  final case class CreateMovieLinks(replyTo: ActorRef[Boolean]) extends Command
  final case class ReadMovieLinks(
      movieIds: Seq[Int],
      replyTo: ActorRef[MovieLinksResponse]
  ) extends Command
  final case class MovieLinksResponse(movieLinks: Seq[Int])

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateMovieLinks(replyTo) => {
        createLinksFromTable()
        Behaviors.same
      }
      case ReadMovieLinks(movieIds, replyTo) => {
        Behaviors.same
      }
    }

  def createLinksFromTable(): Unit = {
    val df = linksTable.readData()
    df.foreach { (row) => }
  }
}
