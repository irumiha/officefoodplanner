package org.codecannery.lunchplanner.domain.users

import cats._
import cats.data.EitherT
import cats.implicits._
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserValidationError, UserNotFoundError}

class UserValidationInterpreter[F[_]: Monad](userRepo: UserRepositoryAlgebra[F]) extends UserValidationAlgebra[F] {
  def doesNotExist(user: User) = EitherT {
    userRepo.findByUserName(user.userName).map {
      case None => Right(())
      case Some(_) => Left(UserAlreadyExistsError(user))
    }
  }

  def exists(userId: Option[Long]): EitherT[F, UserNotFoundError.type, User] =
    EitherT {
      userId.map { id =>
        userRepo.get(id).map {
          case Some(u) => Right((u))
          case _ => Left(UserNotFoundError)
        }
      }.getOrElse(
        Either.left[UserNotFoundError.type, User](UserNotFoundError).pure[F]
      )
    }

  def validChanges(storedUser: User, newUser: User): EitherT[F, UserValidationError, User] =
    EitherT {
      Either.right[UserValidationError, User](newUser).pure[F]
    }
}

object UserValidationInterpreter {
  def apply[F[_]: Monad](repo: UserRepositoryAlgebra[F]): UserValidationAlgebra[F] =
    new UserValidationInterpreter[F](repo)
}
