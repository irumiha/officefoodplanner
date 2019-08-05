package com.officefoodplanner

import cats.data.{Kleisli, OptionT}
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.infrastructure.endpoint.{AuthenticationEndpoints, StaticEndpoints, UserEndpoints, UtilityEndpoints}
import com.officefoodplanner.infrastructure.middleware.Authenticate
import com.officefoodplanner.infrastructure.repository.postgres.{SessionTableRepository, UserTableRepository}
import com.officefoodplanner.infrastructure.service.authentication.DoobieAuthenticationService
import com.officefoodplanner.infrastructure.service.user.DoobieUserService
import doobie.ConnectionIO
import org.http4s.{HttpRoutes, Request, Response}
import tsec.passwordhashers.PasswordHasher
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.ExecutionContext

class ApplicationModule[F[_] : ContextShift : ConcurrentEffect : Timer](
  config: ApplicationConfig,
  xa: doobie.Transactor[F],
  blockingIoEc: ExecutionContext
) {
  val cryptService: PasswordHasher[ConnectionIO, BCrypt] =  BCrypt.syncPasswordHasher[ConnectionIO]
  val userRepo: UserTableRepository =  new UserTableRepository()
  val userService: DoobieUserService[F, BCrypt] =  new DoobieUserService[F, BCrypt](userRepo, cryptService, config, xa)
  val sessionRepo: SessionTableRepository =  new SessionTableRepository
  val authService: DoobieAuthenticationService[F, BCrypt] =  new DoobieAuthenticationService[F, BCrypt](
    config,
    sessionRepo,
    userRepo,
    xa,
    cryptService
  )
  val authMiddleware: Authenticate[F, doobie.ConnectionIO, BCrypt] =  new Authenticate(config, authService)

  val userEndpoints: HttpRoutes[F] = UserEndpoints.endpoints(userService, authMiddleware)
  val authEndpoints: HttpRoutes[F] = AuthenticationEndpoints.endpoints(config, authService)
  val utilityEndpoints: HttpRoutes[F] = UtilityEndpoints.endpoints()
  val staticEndpoints: Kleisli[OptionT[F, *], Request[F], Response[F]] = StaticEndpoints.endpoints(blockingIoEc)

}
