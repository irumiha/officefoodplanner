package org.codecannery.lunchplanner.infrastructure.service.user

import java.util.UUID

import doobie.ConnectionIO
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.domain.user.command.UpdateUser
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserAlreadyExistsError, UserNotFoundError, UserRepository, UserValidation, UserValidationError}

class UserValidationInterpreter(userRepo: UserRepository[ConnectionIO]) extends UserValidation[ConnectionIO] {

  def doesNotExist(userName: String): ConnectionIO[Either[UserAlreadyExistsError, Unit]] =
    userRepo.findByUsername(userName).map {
      case None    => Right(())
      case Some(_) => Left(UserAlreadyExistsError(userName))
    }

  def exists(userId: UUID): ConnectionIO[Either[UserNotFoundError.type, User]] =
    userRepo.get(userId).map {
      case Some(u) => Right(u)
      case _       => Left(UserNotFoundError)
    }

  def validChanges(storedUser: User, newUser: UpdateUser): Either[UserValidationError, User] = {
    val changed = newUser.into[User].transform
    Right[UserValidationError, User](changed)
  }
}

object UserValidationInterpreter {
  def apply(repo: UserRepository[ConnectionIO]): UserValidation[ConnectionIO] =
    new UserValidationInterpreter(repo)
}
