package org.codecannery.lunchplanner.domain.user

import cats.Applicative
import cats.data.EitherT
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.domain.user.command.UpdateUser
import org.codecannery.lunchplanner.domain.user.model.User

trait UserValidation[F[_]] {

  def userMustNotExist(user: Option[User])(implicit AP: Applicative[F]): EitherT[F, UserValidationError, Unit] = {
    val validation = user match {
      case Some(u) => Left(UserAlreadyExistsError(u.userName))
      case None    => Right(())
    }

    EitherT.fromEither[F](validation)
  }

  def userMustExist(user: Option[User])(implicit AP: Applicative[F]): EitherT[F, UserValidationError, User] = {
    val validation = user match {
      case Some(u) => Right(u)
      case None    => Left(UserNotFoundError)
    }
    EitherT.fromEither[F](validation)
  }

  def validChanges(storedUser: User, newUser: UpdateUser)(
      implicit AP: Applicative[F]): EitherT[F, UserValidationError, User] = {
    val changed = newUser.into[User].transform
    val validation = Right[UserValidationError, User](changed)

    EitherT.fromEither[F](validation)
  }

}
