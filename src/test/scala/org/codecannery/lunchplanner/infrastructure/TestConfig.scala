package org.codecannery.lunchplanner.infrastructure

import cats.implicits._
import cats.effect.{Effect, IO}
import io.circe.config.parser
import org.codecannery.lunchplanner.config.ApplicationConfig

object TestConfig {

  def initializedConfig[F[_]: Effect ]: F[ApplicationConfig] =
    for {
      appConfig <- parser.decodePathF[F, ApplicationConfig]("application")
    } yield appConfig

  val appTestConfig: ApplicationConfig = initializedConfig[IO].unsafeRunSync()

}
