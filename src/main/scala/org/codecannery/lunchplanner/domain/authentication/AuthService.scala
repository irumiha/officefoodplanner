package org.codecannery.lunchplanner.domain.authentication

import cats.Monad
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserValidation}

class AuthService[F[_]: Monad](
    userRepo: UserRepository[ConnectionIO],
    validation: UserValidation[ConnectionIO],
    xa: Transactor[F]
) {

  def authenticated = ???

}
