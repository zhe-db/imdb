package edu.duke.imdb.tmdb.client

import scala.concurrent.duration._

package object client {
  val RequestRateLimitDelay: FiniteDuration = 11000000.seconds
  val Port: Int = 80
}
