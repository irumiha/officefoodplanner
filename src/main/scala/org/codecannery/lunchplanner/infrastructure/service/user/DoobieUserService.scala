package org.codecannery.lunchplanner.infrastructure.service.user

import cats.Monad
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserService, UserValidation}

class DoobieUserService[F[_] : Monad](
  userRepo: UserRepository[ConnectionIO],
  validation: UserValidation[ConnectionIO],
  xa: Transactor[F]
) extends UserService[F, ConnectionIO](userRepo, validation) {
  override def transact[A](t: ConnectionIO[A]): F[A] = doobie.implicits.toConnectionIOOps(t).transact(xa)
}

