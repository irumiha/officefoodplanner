package org.codecannery.lunchplanner.domain.users

import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import io.bfil.automapper.automap
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

object UserValidationInterpreter extends UserValidationAlgebra {
  def doesNotExist(userName: String) = EitherT {
    userRepo.findByUserName(userName).map {
      case None => Right(())
      case Some(_) => Left(UserAlreadyExistsError(userName))
    }
  }

  def exists(userId: UUID): EitherT[F, UserNotFoundError.type, User] =
    EitherT {
      userRepo.get(userId).map {
        case Some(u) => Right(u)
        case _ => Left(UserNotFoundError)
      }
    }

  def validChanges(storedUser: User, newUser: UpdateUser): Either[UserValidationError, User] = {
    val changed = automap(newUser).to[User]
    Either.right[UserValidationError, User](changed)
  }
}
