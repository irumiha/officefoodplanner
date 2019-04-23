package org.codecannery.lunchplanner

import cats.effect._
import cats.implicits._

import doobie.util.ExecutionContexts
import io.circe.config.parser
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import org.http4s.server.blaze.BlazeServerBuilder

object ApplicationServer extends IOApp {

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, Server[F]] = {

    for {
      conf           <- Resource.liftF(parser.decodePathF[F, config.ApplicationConfig]("application"))
      connEc         <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc          <- ExecutionContexts.cachedThreadPool[F]
      xa             <- config.DatabaseConfig.dbTransactor[F](conf.db, connEc, txnEc)
      app            = new ApplicationModule(conf, xa)
      httpApp        =  Router(
        "/users" -> app.userEndpoints,
        "/auth"  -> app.authEndpoints
      ).orNotFound
      _              <- Resource.liftF[F, Unit](config.DatabaseConfig.initializeDb[F](conf.db))
      server <-
        BlazeServerBuilder[F]
          .bindHttp(conf.server.port, conf.server.host)
          .withHttpApp(httpApp)
          .resource
    } yield server

  }

  def run(args : List[String]) : IO[ExitCode] = createServer[IO].use(_ => IO.never).as(ExitCode.Success)
}
