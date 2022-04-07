package edu.duke.imdb.distributedCache

import edu.duke.imdb.memStore.ReplicatedCache
import akka.cluster.ddata.Replicator
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ddata.LWWMap
import akka.cluster.ddata.LWWMapKey
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.SelfUniqueAddress
import akka.cluster.ddata.typed.scaladsl.DistributedData
import akka.cluster.ddata.typed.scaladsl.Replicator.{Update, Get}
import akka.actor.{Actor, Props, ActorLogging}

object MovieLinksCache {
  val name = "movieLinks"
  val durable = true
  val writeConsistency = Replicator.WriteLocal
  val readConsistency = Replicator.ReadLocal
  sealed trait Command
  sealed trait Response
  final case class Cached(key: String, value: Option[String]) extends Response

  final case class PutInCache(key: String, value: String) extends Command
  final case class GetFromCache(key: String, replyTo: ActorRef[Cached])
      extends Command
  final case class Evict(key: String) extends Command

  private sealed trait InternalCommand extends Command
  private case class InternalGetResponse(
      key: String,
      replyTo: ActorRef[Cached],
      rsp: GetResponse[LWWMap[String, String]]
  ) extends InternalCommand
  private case class InternalUpdateResponse(
      rsp: UpdateResponse[LWWMap[String, String]]
  ) extends InternalCommand

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    DistributedData
      .withReplicatorMessageAdapter[Command, LWWMap[String, String]] {
        replicator =>
          implicit val node: SelfUniqueAddress =
            DistributedData(context.system).selfUniqueAddress

          def dataKey(entryKey: String): LWWMapKey[String, String] =
            if (durable) {
              LWWMapKey(
                "cache-" + name + "-" + math.abs(entryKey.hashCode % 100)
              )
            } else {
              LWWMapKey(
                "durable-" + name + "-" + math.abs(entryKey.hashCode % 100)
              )
            }

          Behaviors.receiveMessage[Command] {
            case PutInCache(key, value) =>
              replicator.askUpdate(
                askReplyTo =>
                  Update(
                    dataKey(key),
                    LWWMap.empty[String, String],
                    writeConsistency,
                    askReplyTo
                  )(_ :+ (key -> value)),
                InternalUpdateResponse.apply
              )

              Behaviors.same

            case Evict(key) =>
              replicator.askUpdate(
                askReplyTo =>
                  Update(
                    dataKey(key),
                    LWWMap.empty[String, String],
                    writeConsistency,
                    askReplyTo
                  )(_.remove(node, key)),
                InternalUpdateResponse.apply
              )

              Behaviors.same

            case GetFromCache(key, replyTo) =>
              replicator.askGet(
                askReplyTo => Get(dataKey(key), readConsistency, askReplyTo),
                rsp => InternalGetResponse(key, replyTo, rsp)
              )

              Behaviors.same

            case InternalGetResponse(key, replyTo, g @ GetSuccess(_, _)) =>
              replyTo ! Cached(key, g.dataValue.get(key))
              Behaviors.same

            case InternalGetResponse(key, replyTo, _: NotFound[_]) =>
              replyTo ! Cached(key, None)
              Behaviors.same

            case _: InternalGetResponse    => Behaviors.same // ok
            case _: InternalUpdateResponse => Behaviors.same // ok
          }
      }
  }
}

object MovieTestCache
    extends ReplicatedCache[String](
      name = "movieLinks",
      durable = true,
      writeConsistency = Replicator.WriteLocal,
      readConsistency = Replicator.ReadLocal
    ) {}
