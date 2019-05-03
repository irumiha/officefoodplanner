package org.codecannery.lunchplanner.infrastructure.repository

import cats.implicits._
import cats.effect.{Async, ContextShift, Effect, IO}
import _root_.doobie.Transactor
import io.circe.config.parser
import org.codecannery.lunchplanner.config.{DatabaseConfig, ApplicationConfig}

import scala.concurrent.ExecutionContext

package object postgres {
  def getTransactor[F[_] : Async : ContextShift](cfg : DatabaseConfig) : Transactor[F] =
    Transactor.fromDriverManager[F](
      cfg.driver,            // driver classname
      cfg.url,               // connect URL (driver-specific)
      cfg.user,              // user
      cfg.password           // password
    )

  /*
   * Provide a transactor for testing once schema has been migrated.
   */
  def initializedTransactor[F[_] : Effect : Async : ContextShift] : F[Transactor[F]] = for {
    appConfig <- parser.decodePathF[F, ApplicationConfig]("application")
    _         <- DatabaseConfig.initializeDb(appConfig.db)
  } yield getTransactor(appConfig.db)

  lazy val testEc: ExecutionContext = ExecutionContext.Implicits.global

  implicit lazy val testCs: ContextShift[IO] = IO.contextShift(testEc)

  lazy val testTransactor: Transactor[IO] = initializedTransactor[IO].unsafeRunSync()
}
