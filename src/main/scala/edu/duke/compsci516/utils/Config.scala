package edu.duke.compsci516.components

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigComponent {
  val config: Config = ConfigFactory.load()
}
