package edu.duke.imdb.components

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigComponent {
  val config: Config = ConfigFactory.load()
}
