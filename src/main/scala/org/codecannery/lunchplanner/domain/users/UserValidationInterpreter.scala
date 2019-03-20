package org.codecannery.lunchplanner.domain.users

import java.util.UUID

import doobie.ConnectionIO
import io.bfil.automapper.automap
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

class UserValidationInterpreter(userRepo: UserRepositoryAlgebra[ConnectionIO])
    extends UserValidationAlgebra[ConnectionIO] {

  def doesNotExist(userName: String): ConnectionIO[Either[UserAlreadyExistsError, Unit]] =
    userRepo.findByUserName(userName).map {
      case None    => Right(())
      case Some(_) => Left(UserAlreadyExistsError(userName))
    }

  def exists(userId: UUID): ConnectionIO[Either[UserNotFoundError.type, User]] =
    userRepo.get(userId).map {
      case Some(u) => Right(u)
      case _       => Left(UserNotFoundError)
    }

  def validChanges(
      storedUser: User,
      newUser: UpdateUser): Either[UserValidationError, User] = {
    val changed = automap(newUser).to[User]
    Right[UserValidationError, User](changed)
  }
}

object UserValidationInterpreter {
  def apply(
      repo: UserRepositoryAlgebra[ConnectionIO]): UserValidationAlgebra[ConnectionIO] =
    new UserValidationInterpreter(repo)
}
