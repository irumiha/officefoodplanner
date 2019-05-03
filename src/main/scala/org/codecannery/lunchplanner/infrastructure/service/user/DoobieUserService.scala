package org.codecannery.lunchplanner.infrastructure.service.user

import cats.effect.Bracket
import doobie._
import doobie.implicits._
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserService}
import tsec.passwordhashers.PasswordHasher

class DoobieUserService[F[_], H](
    val userRepo: UserRepository[ConnectionIO],
    val cryptService: PasswordHasher[ConnectionIO, H],
    xa: Transactor[F]
)(implicit B: Bracket[F, Throwable]) extends UserService[F, ConnectionIO, H] {
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}
