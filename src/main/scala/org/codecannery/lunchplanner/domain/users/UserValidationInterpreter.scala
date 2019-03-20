package org.codecannery.lunchplanner.domain.users

import java.util.UUID

import cats._
import cats.implicits._
import io.bfil.automapper.automap
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

class UserValidationInterpreter[F[_]: Monad](userRepo: UserRepositoryAlgebra[F])
    extends UserValidationAlgebra[F] {

  def doesNotExist(userName: String): F[Either[UserAlreadyExistsError, Unit]] =
    userRepo.findByUserName(userName).map {
      case None    => Right(())
      case Some(_) => Left(UserAlreadyExistsError(userName))
    }


  def exists(userId: UUID): F[Either[UserNotFoundError.type, User]] =
    userRepo.get(userId).map {
      case Some(u) => Right(u)
      case _       => Left(UserNotFoundError)
    }

  def validChanges(storedUser: User, newUser: UpdateUser): F[Either[UserValidationError, User]] = {
      val changed = automap(newUser).to[User]
      Either.right[UserValidationError, User](changed).pure[F]
    }
}

object UserValidationInterpreter {
  def apply[F[_]: Monad](repo: UserRepositoryAlgebra[F]): UserValidationAlgebra[F] =
    new UserValidationInterpreter[F](repo)
}
