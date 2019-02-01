package org.codecannery.lunchplanner.config

final case class ServerConfig(host: String, port: Int)
final case class LunchPlannerConfig(db: DatabaseConfig, server: ServerConfig)
