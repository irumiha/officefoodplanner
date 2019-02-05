package org.codecannery.lunchplanner.domain.users
import cats.data.EitherT
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

trait UserValidationAlgebra[F[_]] {

  def doesNotExist(userName: String): EitherT[F, UserAlreadyExistsError, Unit]

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, User]

  def validChanges(storedUser: User, newUser: UpdateUser): EitherT[F, UserValidationError, User]

}
