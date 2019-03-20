package org.codecannery.lunchplanner.domain.users
import java.util.UUID

import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

trait UserValidationAlgebra[F[_]] {

  def doesNotExist(userName: String): F[Either[UserAlreadyExistsError, Unit]]

  def exists(userId: UUID): F[Either[UserNotFoundError.type, User]]

  def validChanges(storedUser: User, newUser: UpdateUser): Either[UserValidationError, User]

}
