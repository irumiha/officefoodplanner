package org.codecannery.lunchplanner.domain.users

import cats._
import cats.data.EitherT
import cats.implicits._
import io.bfil.automapper.automap
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

class UserValidationInterpreter[F[_]: Monad](userRepo: UserRepositoryAlgebra[F]) extends UserValidationAlgebra[F] {
  def doesNotExist(userName: String) = EitherT {
    userRepo.findByUserName(userName).map {
      case None => Right(())
      case Some(_) => Left(UserAlreadyExistsError(userName))
    }
  }

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, User] =
    EitherT {
      userId.map { id =>
        userRepo.get(id).map {
          case Some(u) => Right(u)
          case _ => Left(UserNotFoundError)
        }
      }.getOrElse(
        Either.left[UserNotFoundError.type, User](UserNotFoundError).pure[F]
      )
    }

  def validChanges(storedUser: User, newUser: UpdateUser): EitherT[F, UserValidationError, User] =
    EitherT {
      val changed = automap(newUser).to[User]
      Either.right[UserValidationError, User](changed).pure[F]
    }
}

object UserValidationInterpreter {
  def apply[F[_]: Monad](repo: UserRepositoryAlgebra[F]): UserValidationAlgebra[F] =
    new UserValidationInterpreter[F](repo)
}
