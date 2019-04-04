package org.codecannery.lunchplanner.infrastructure.service.authentication

import cats._
import doobie._
import doobie.implicits._
import org.codecannery.lunchplanner.domain.authentication.AuthenticationService
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserValidation}

class DoobieAuthenticationService[F[_] : Monad](
  userRepo: UserRepository[ConnectionIO],
  validation: UserValidation[ConnectionIO],
  xa: Transactor[F]
) extends AuthenticationService[F, ConnectionIO](userRepo, validation) {
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}
