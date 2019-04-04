package org.codecannery.lunchplanner.domain.authentication

import cats._
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserValidation}
import org.codecannery.lunchplanner.infrastructure.service.TransactingService

abstract class AuthenticationService[F[_]: Monad, D[_]: Monad](
    userRepo: UserRepository[D],
    validation: UserValidation[D]
) extends TransactingService[F, D] {

  def newSession(user: User) = {

  }

  def authenticated = ???

}
