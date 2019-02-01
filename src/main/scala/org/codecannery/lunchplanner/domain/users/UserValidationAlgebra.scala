package org.codecannery.lunchplanner.domain.users
import cats.data.EitherT
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserValidationError, UserNotFoundError}

trait UserValidationAlgebra[F[_]] {

  def doesNotExist(user: User): EitherT[F, UserAlreadyExistsError, Unit]

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, User]

  def validChanges(storedUser: User, newUser: User): EitherT[F, UserValidationError, User]

}
