package org.codecannery.lunchplanner.infrastructure.service.user

import cats._
import doobie._
import doobie.implicits._

import org.codecannery.lunchplanner.domain.user.{UserRepository, UserService, UserValidation}

class DoobieUserService[F[_] : Monad](
  override val userRepo: UserRepository[ConnectionIO],
  override val validation: UserValidation[ConnectionIO],
  xa: Transactor[F]
) extends UserService[F, ConnectionIO]
{
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}

