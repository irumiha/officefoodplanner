package org.codecannery.lunchplanner

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import io.circe.config.parser
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import org.http4s.server.blaze.BlazeServerBuilder
import tsec.passwordhashers.jca.BCrypt
import infrastructure.endpoint.{AuthenticationEndpoints, UserEndpoints}
import org.codecannery.lunchplanner.infrastructure.middleware.Authenticate
import org.codecannery.lunchplanner.infrastructure.repository.postgres.{SessionJsonRepository, UserJsonRepository}
import org.codecannery.lunchplanner.infrastructure.service.authentication.DoobieAuthenticationService
import org.codecannery.lunchplanner.infrastructure.service.user.DoobieUserService

object ApplicationServer extends IOApp {

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, Server[F]] = {

    for {
      conf           <- Resource.liftF(parser.decodePathF[F, config.ApplicationConfig]("application"))
      connEc         <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc          <- ExecutionContexts.cachedThreadPool[F]
      xa             <- config.DatabaseConfig.dbTransactor[F](conf.db, connEc, txnEc)
      cryptService   =  BCrypt.syncPasswordHasher[ConnectionIO]
      userRepo       =  new UserJsonRepository()
      userService    =  new DoobieUserService[F, BCrypt](userRepo, cryptService, xa)
      sessionRepo    =  new SessionJsonRepository
      authService    =  new DoobieAuthenticationService[F, BCrypt](
        conf,
        sessionRepo,
        userRepo,
        xa,
        cryptService
      )
      authMiddleware =  new Authenticate(conf, authService)
      httpApp        =  Router(
        "/users" -> UserEndpoints.endpoints[F, ConnectionIO, BCrypt](userService, authMiddleware),
        "/auth"  -> AuthenticationEndpoints.endpoints[F, ConnectionIO, BCrypt](conf, authService)
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
