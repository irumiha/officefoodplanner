package com.officefoodplanner.config

import cats.syntax.functor._
import cats.effect.{Async, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import doobie.hikari.HikariTransactor.initial
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)
case class DatabaseConfig(url: String, driver: String, user: String, password: String, connections: DatabaseConnectionsConfig)

object DatabaseConfig {
  def dbTransactor[F[_]: Async : ContextShift](
    dbc: DatabaseConfig,
    connEc : ExecutionContext,
    transEc : ExecutionContext
  ): Resource[F, HikariTransactor[F]] =
    for {
      _ <- Resource.liftF(Async[F].delay(Class.forName(dbc.driver)))
      t <- initial[F](connEc, transEc)
      _ <- Resource.liftF {
        t.configure { ds =>
          Async[F].delay {
            ds setJdbcUrl         dbc.url
            ds setUsername        dbc.user
            ds setPassword        dbc.password
            ds setMaximumPoolSize dbc.connections.poolSize
          }
        }
      }
    } yield t

  /**
    * Runs the flyway migrations against the target database
    */
  def initializeDb[F[_]](cfg : DatabaseConfig)(implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway = {
        Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
      }
      fw.migrate()
    }.as(())
}
