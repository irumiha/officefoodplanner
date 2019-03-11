package org.codecannery.lunchplanner
import io.circe.config.parser
import org.http4s.server.{Router, Server}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import cats.effect._
import cats.implicits._
import config._
import domain.users._
import doobie.util.ExecutionContexts
import infrastructure.endpoint.UserEndpoints
import infrastructure.repository.doobie.DoobieUserRepositoryInterpreter
import tsec.passwordhashers.jca.BCrypt

object LunchPlannerServer extends IOApp {

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer]: Resource[F, Server[F]] = {
    for {
      conf           <- Resource.liftF(parser.decodePathF[F, LunchPlannerConfig]("lunchplanner"))
      connEc         <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc          <- ExecutionContexts.cachedThreadPool[F]
      xa             <- DatabaseConfig.dbTransactor(conf.db, connEc, txnEc)
      userRepo       =  DoobieUserRepositoryInterpreter[F](xa)
      userValidation =  UserValidationInterpreter[F](userRepo)
      userService    =  UserService[F](userRepo, userValidation)
      services       =  UserEndpoints.endpoints[F, BCrypt](userService, BCrypt.syncPasswordHasher[F])
      httpApp        =  Router("/" -> services).orNotFound
      _              <- Resource.liftF(DatabaseConfig.initializeDb(conf.db))
      server <-
        BlazeServerBuilder[F]
          .bindHttp(conf.server.port, conf.server.host)
          .withHttpApp(httpApp)
          .resource
    } yield server

  }

  def run(args : List[String]) : IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)
}
