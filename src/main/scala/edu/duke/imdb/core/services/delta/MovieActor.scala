package edu.duke.imdb.core.services.delta

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.apache.spark.sql.DataFrame

import _root_.edu.duke.imdb.data.delta.tables.MoviedDetailDeltaTable
import _root_.edu.duke.imdb.models.delta._
import _root_.edu.duke.imdb.components.SparkComponent
import _root_.edu.duke.imdb.models.entity.MovieDetailRow
import edu.duke.imdb.components.ConfigComponent

object MovieDetailTableActor extends ConfigComponent {
  val deltaTable = new MoviedDetailDeltaTable()
  import deltaTable.spark.implicits._
  val batchSize = this.config.getInt("delta.batch_write.batch_size")

  sealed trait Command

  final case class AddMovie(
      movid: MovieDetailRow,
      replyTo: ActorRef[AddMovieResponse]
  ) extends Command

  final case class AddMovieResponse(result: Boolean) extends Command
  def apply(): Behavior[Command] = dataFrames(
    Seq.empty[DeltaMovieDetailRow]
  )

  def dataFrames(movieSeq: Seq[DeltaMovieDetailRow]): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case AddMovie(movie, replyTo) => {
          if (movieSeq.length >= batchSize) {
            deltaTable.batchWrite(movieSeq.toDF())
            dataFrames(Seq.empty[DeltaMovieDetailRow])
          } else {
            dataFrames(movieSeq :+ new DeltaMovieDetailRow(movie))
          }
        }

        case _ => Behaviors.same
      }
    }
  }

}
