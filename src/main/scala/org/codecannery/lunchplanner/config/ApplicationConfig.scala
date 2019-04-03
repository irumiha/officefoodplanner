package org.codecannery.lunchplanner.config

final case class ServerConfig(host: String, port: Int)
final case class AuthConfig(sessionLength: Long, coookieSignKey: String)
final case class CsrfConfig(secret: String)
final case class ApplicationConfig(
  db: DatabaseConfig,
  server: ServerConfig,
  auth: AuthConfig,
  csrf: CsrfConfig
)
