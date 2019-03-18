package org.codecannery.lunchplanner.domain.users
import java.util.UUID

import cats.data.EitherT
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

trait UserValidationAlgebra[F[_]] {

  def doesNotExist(userName: String): EitherT[F, UserAlreadyExistsError, Unit]

  def exists(userId: UUID): EitherT[F, UserNotFoundError.type, User]

  def validChanges(storedUser: User, newUser: UpdateUser): EitherT[F, UserValidationError, User]

}
