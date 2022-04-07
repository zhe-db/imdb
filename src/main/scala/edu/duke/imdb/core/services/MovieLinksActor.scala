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
import akka.actor.ActorContext
import akka.actor.ActorRefFactory
import akka.actor.typed.scaladsl
import akka.actor.typed

object MovieLinksActor extends ConfigComponent {
  sealed trait Command
  val linksTable = new MovieLensLinksDeltaTable()

  final case class CreateMovieLinks() extends Command
  final case class ReadMovieLinks(
      movieIds: Seq[Int],
      replyTo: ActorRef[MovieLinksResponse]
  ) extends Command
  final case class MovieLinksResponse(movieLinks: Seq[Int])
  private final case class WrappedStoredResponse(
      response: MovieLinksCache.Response
  ) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val link2idStore = context.spawn(MovieLinksCache(), "link2idStore")
      val id2linkStore = context.spawn(MovieLinksCache(), "id2linkStore")

      val storeResponseMapper: ActorRef[MovieLinksCache.Response] =
        context.messageAdapter(rsp => WrappedStoredResponse(rsp))

      Behaviors.receiveMessage {
        case CreateMovieLinks() => {
          createLinksFromTable(link2idStore, id2linkStore, storeResponseMapper)
          Behaviors.same
        }

        case ReadMovieLinks(movieIds, replyTo) => {
          Behaviors.same
        }

        case wrapped: WrappedStoredResponse =>
          wrapped.response match {
            case MovieLinksCache.Cached(key, value) =>
              println(key, value)
              Behaviors.same
          }
      }
    }

  def createLinksFromTable(
      link2idStore: ActorRef[MovieLinksCache.Command],
      id2linkStore: ActorRef[MovieLinksCache.Command],
      source: ActorRef[MovieLinksCache.Response]
  ): Unit = {
    val df = linksTable.readData()
    df.foreach { (row) =>
      val movieLinkId = row.getString(0)
      val movieId = row.getString(2)
      link2idStore ! MovieLinksCache.PutInCache(movieLinkId, movieId)
    }
  }
}

object MovieLinksActorObj {
  def main(args: Array[String]): Unit = {
    val system: typed.ActorSystem[MovieLinksActor.Command] =
      typed.ActorSystem(MovieLinksActor(), "movieLinks")

    system ! MovieLinksActor.CreateMovieLinks()
  }
}
