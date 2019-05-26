package com.officefoodplanner.infrastructure.service.authentication

import cats.effect.Bracket
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.repository.{SessionRepository, UserRepository}
import com.officefoodplanner.domain.auth.AuthenticationService
import doobie._
import doobie.implicits._
import tsec.passwordhashers.PasswordHasher

class DoobieAuthenticationService[F[_], H](
    val applicationConfig: ApplicationConfig,
    val sessionRepository: SessionRepository[ConnectionIO],
    val userRepository: UserRepository[ConnectionIO],
    xa: Transactor[F],
    val cryptService: PasswordHasher[ConnectionIO, H]
)(implicit B: Bracket[F, Throwable]) extends AuthenticationService[F, ConnectionIO, H] {
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}
