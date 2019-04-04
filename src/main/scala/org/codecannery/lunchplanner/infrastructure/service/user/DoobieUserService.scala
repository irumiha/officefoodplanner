package org.codecannery.lunchplanner.infrastructure.service.user

import cats._
import doobie._
import doobie.implicits._

import org.codecannery.lunchplanner.domain.user.{UserRepository, UserService, UserValidation}

class DoobieUserService[F[_] : Monad](
  userRepo: UserRepository[ConnectionIO],
  validation: UserValidation[ConnectionIO],
  xa: Transactor[F]
) extends UserService[F, ConnectionIO](userRepo, validation) {
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}

