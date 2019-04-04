package org.codecannery.lunchplanner

import io.circe.config.parser
import org.http4s.server.{Router, Server}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import cats.effect._
import cats.implicits._
import doobie.ConnectionIO
import doobie.util.ExecutionContexts
import infrastructure.endpoint.UserEndpoints
import org.codecannery.lunchplanner.infrastructure.repository.postgres.UserJsonRepository
import org.codecannery.lunchplanner.infrastructure.service.user.{DoobieUserService, UserValidationInterpreter}
import tsec.passwordhashers.jca.BCrypt

object ApplicationServer extends IOApp {

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, Server[F]] = {

    for {
      conf           <- Resource.liftF(parser.decodePathF[F, config.ApplicationConfig]("application"))
      connEc         <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc          <- ExecutionContexts.cachedThreadPool[F]
      xa             <- config.DatabaseConfig.dbTransactor(conf.db, connEc, txnEc)
      userRepo       =  new UserJsonRepository()
      userValidation =  UserValidationInterpreter(userRepo)
      userService    =  new DoobieUserService[F](userRepo, userValidation, xa)
      services       =  UserEndpoints.endpoints[F, BCrypt, ConnectionIO](conf, userService, BCrypt.syncPasswordHasher[F])
      httpApp        =  Router("/" -> services).orNotFound
      _              <- Resource.liftF(config.DatabaseConfig.initializeDb(conf.db))
      server <-
        BlazeServerBuilder[F]
          .bindHttp(conf.server.port, conf.server.host)
          .withHttpApp(httpApp)
          .resource
    } yield server

  }

  def run(args : List[String]) : IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)
}
