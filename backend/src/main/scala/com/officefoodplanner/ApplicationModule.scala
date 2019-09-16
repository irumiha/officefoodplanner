package com.officefoodplanner

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import doobie.ConnectionIO
import tsec.passwordhashers.jca.BCrypt

import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.infrastructure.endpoint.{AuthenticationEndpoints, StaticEndpoints, UserEndpoints, UtilityEndpoints}
import com.officefoodplanner.infrastructure.middleware.Authenticate
import com.officefoodplanner.infrastructure.repository.postgres.{SessionTableRepository, UserTableRepository}
import com.officefoodplanner.infrastructure.service.authentication.DoobieAuthenticationService
import com.officefoodplanner.infrastructure.service.user.DoobieUserService

import scala.concurrent.ExecutionContext

//noinspection TypeAnnotation
class ApplicationModule[F[_] : ContextShift : ConcurrentEffect : Timer](
  config: ApplicationConfig,
  xa: doobie.Transactor[F],
  blockingIoEc: ExecutionContext
) {
  private val cryptService =  BCrypt.syncPasswordHasher[ConnectionIO]
  private val userRepo = UserTableRepository
  val userService: DoobieUserService[F, BCrypt] =  new DoobieUserService[F, BCrypt](userRepo, cryptService, config, xa)
  private val sessionRepo = SessionTableRepository
  val authService: DoobieAuthenticationService[F, BCrypt] =  new DoobieAuthenticationService[F, BCrypt](
    config,
    sessionRepo,
    userRepo,
    xa,
    cryptService
  )
  val authMiddleware: Authenticate[F, doobie.ConnectionIO, BCrypt] =  new Authenticate(config, authService)

  val userEndpoints    = UserEndpoints.endpoints(userService, authMiddleware)
  val authEndpoints    = AuthenticationEndpoints.endpoints(config, authService)
  val utilityEndpoints = UtilityEndpoints.endpoints()
  val staticEndpoints  = StaticEndpoints.endpoints(blockingIoEc)

}
