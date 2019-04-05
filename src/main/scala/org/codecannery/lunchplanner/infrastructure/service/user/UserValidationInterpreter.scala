package org.codecannery.lunchplanner.infrastructure.service.user

import java.util.UUID

import cats.Functor
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.domain.user.command.UpdateUser
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserAlreadyExistsError, UserNotFoundError, UserRepository, UserValidation, UserValidationError}

class UserValidationInterpreter[D[_]](userRepo: UserRepository[D])(implicit F: Functor[D]) extends UserValidation[D] {

  def doesNotExist(userName: String): D[Either[UserAlreadyExistsError, Unit]] =
    F.map(userRepo.findByUsername(userName)) {
      case None    => Right(())
      case Some(_) => Left(UserAlreadyExistsError(userName))
    }

  def exists(userId: UUID): D[Either[UserNotFoundError.type, User]] =
    F.map(userRepo.get(userId)) {
      case Some(u) => Right(u)
      case _       => Left(UserNotFoundError)
    }

  def validChanges(storedUser: User, newUser: UpdateUser): Either[UserValidationError, User] = {
    val changed = newUser.into[User].transform
    Right[UserValidationError, User](changed)
  }
}

object UserValidationInterpreter {
  def apply[D[_]: Functor](repo: UserRepository[D]): UserValidation[D] =
    new UserValidationInterpreter(repo)
}
