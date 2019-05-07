package com.officefoodplanner.infrastructure

import cats.implicits._
import cats.effect.{Effect, IO}
import com.officefoodplanner.config.ApplicationConfig
import io.circe.config.parser

object TestConfig {

  def initializedConfig[F[_]: Effect ]: F[ApplicationConfig] =
    for {
      appConfig <- parser.decodePathF[F, ApplicationConfig]("application")
    } yield appConfig

  val appTestConfig: ApplicationConfig = initializedConfig[IO].unsafeRunSync()

}
