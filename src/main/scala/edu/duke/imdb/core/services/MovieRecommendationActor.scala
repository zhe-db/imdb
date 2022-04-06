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

object MovieRecommendationsActor extends ConfigComponent {
  sealed trait Command

  final case class GetRecommendedMovies(
      userId: java.util.UUID,
      replyTo: ActorRef[GetRecommendationResponse]
  ) extends Command

  final case class TrainRecommendationModel(replyTo: ActorRef[Boolean])
      extends Command

  final case class GetRecommendationResponse(movies: Seq[Int])

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val movieLinksActor = context.spawn(MovieLinksActor(), "movieLinksActor")
      Behaviors.receiveMessage { message =>
        Behaviors.same
      }
    }
}
