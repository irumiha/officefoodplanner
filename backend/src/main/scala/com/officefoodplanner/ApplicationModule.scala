package com.officefoodplanner

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.infrastructure.endpoint.{AuthenticationEndpoints, StaticEndpoints, UserEndpoints, UtilityEndpoints}
import com.officefoodplanner.infrastructure.middleware.Authenticate
import com.officefoodplanner.infrastructure.repository.postgres.{SessionTableRepository, UserTableRepository}
import com.officefoodplanner.infrastructure.service.authentication.DoobieAuthenticationService
import com.officefoodplanner.infrastructure.service.user.DoobieUserService
import doobie.ConnectionIO
import tsec.passwordhashers.jca.BCrypt

class ApplicationModule[F[_] : ContextShift : ConcurrentEffect : Timer](
  config: ApplicationConfig,
  xa: doobie.Transactor[F]
) {
  val cryptService   =  BCrypt.syncPasswordHasher[ConnectionIO]
  val userRepo       =  new UserTableRepository()
  val userService    =  new DoobieUserService[F, BCrypt](userRepo, cryptService, xa)
  val sessionRepo    =  new SessionTableRepository
  val authService    =  new DoobieAuthenticationService[F, BCrypt](
    config,
    sessionRepo,
    userRepo,
    xa,
    cryptService
  )
  val authMiddleware =  new Authenticate(config, authService)

  val userEndpoints = UserEndpoints.endpoints(userService, authMiddleware)
  val authEndpoints = AuthenticationEndpoints.endpoints(config, authService)
  val utilityEndpoints = UtilityEndpoints.endpoints()
  val staticEndpoints = StaticEndpoints.endpoints()

}
