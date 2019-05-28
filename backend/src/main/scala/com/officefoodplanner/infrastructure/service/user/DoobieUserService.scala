package com.officefoodplanner.infrastructure.service.user

import cats.effect.Bracket
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.UserService
import com.officefoodplanner.domain.auth.repository.UserRepository
import doobie._
import doobie.implicits._
import tsec.passwordhashers.PasswordHasher

class DoobieUserService[F[_], H](
    val userRepo: UserRepository[ConnectionIO],
    val cryptService: PasswordHasher[ConnectionIO, H],
    val applicationConfig: ApplicationConfig,
    xa: Transactor[F]
)(implicit B: Bracket[F, Throwable]) extends UserService[F, ConnectionIO, H] {
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}
