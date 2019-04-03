package org.codecannery.lunchplanner.infrastructure.service.authentication

import cats.Monad
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserValidation}

class AuthService[F[_]: Monad](
    userRepo: UserRepository[ConnectionIO],
    validation: UserValidation[ConnectionIO],
    xa: Transactor[F]
) {
  def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)

  def newSession(user: User) = {

  }

  def authenticated = ???

}
