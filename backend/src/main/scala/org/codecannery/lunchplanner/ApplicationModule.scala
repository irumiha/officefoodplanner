package org.codecannery.lunchplanner

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import doobie.ConnectionIO
import org.codecannery.lunchplanner.config.ApplicationConfig
import org.codecannery.lunchplanner.infrastructure.endpoint.{AuthenticationEndpoints, StaticEndpoints, UserEndpoints, UtilityEndpoints}
import org.codecannery.lunchplanner.infrastructure.middleware.Authenticate
import org.codecannery.lunchplanner.infrastructure.repository.postgres.{SessionTableRepository, UserTableRepository}
import org.codecannery.lunchplanner.infrastructure.service.authentication.DoobieAuthenticationService
import org.codecannery.lunchplanner.infrastructure.service.user.DoobieUserService
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
