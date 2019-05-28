package com.officefoodplanner.config

final case class ServerConfig(host: String, port: Int)
final case class AuthConfig(
  sessionLength: Long,
  coookieSignKey: String,
  cookieDuration: Long,
  sessionCookieName: String,
  minimumPasswordLength: Int
)
final case class CsrfConfig(secret: String)
final case class OauthDetails(
  clientId: String,
  clientSecret: String,
  redirectUri: String,
  tokenUrl: String,
  scope: String,
)
final case class Oauth(
  office365: Option[OauthDetails]
)
final case class ApplicationConfig(
    db: DatabaseConfig,
    server: ServerConfig,
    auth: AuthConfig,
    csrf: CsrfConfig
)
