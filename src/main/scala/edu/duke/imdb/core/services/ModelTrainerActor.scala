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
import _root_.edu.duke.imdb.spark.CollabFilterTrainer

object ModelTrainerActor extends ConfigComponent {
  val collabFilterModelTrainer = CollabFilterTrainer
  sealed trait Command

  final case class TrainCollabModel(
      rank: Int,
      numIterations: Int,
      replyTo: ActorRef[Boolean]
  ) extends Command

  final case class GetRecommendationResponse(movies: Seq[Int])

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage { message =>
        message match {
          case TrainCollabModel(rank, numIterations, replyTo) => {
            collabFilterModelTrainer.trainModel(rank, numIterations)
            replyTo ! true
            Behaviors.same
          }
          case _ => Behaviors.same
        }
      }
    }
}
