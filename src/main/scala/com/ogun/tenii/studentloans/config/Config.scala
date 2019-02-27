package com.ogun.tenii.studentloans.config

import com.typesafe.config.{Config, ConfigFactory}

object Config {

  private[config] val config: Config = ConfigFactory.load()

  val database = config.getStringList("mongo.database").get(0)

}
