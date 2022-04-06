package edu.duke.imdb.distributedCache

import edu.duke.imdb.memStore.ReplicatedCache
import akka.cluster.ddata.Replicator

object MovieLinksCache
    extends ReplicatedCache[String](
      name = "movieLinks",
      durable = true,
      writeConsistency = Replicator.WriteLocal,
      readConsistency = Replicator.ReadLocal
    ) {}
