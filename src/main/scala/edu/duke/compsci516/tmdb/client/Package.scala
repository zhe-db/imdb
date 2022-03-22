package edu.duke.compsci516.tmdb.client

import scala.concurrent.duration._

package object client {
  val RequestRateLimitDelay: FiniteDuration = 11000000.seconds
  val Port: Int = 80
}
