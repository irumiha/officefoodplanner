package org.codecannery.lunchplanner.domain.authentication

import cats.Monad
import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserRepositoryAlgebra, UserValidationAlgebra}


class AuthService[F[_]: Monad](
    userRepo: UserRepositoryAlgebra[ConnectionIO],
    validation: UserValidationAlgebra[ConnectionIO],
    xa: Transactor[F]
) {

  def authenticated = ???

}
