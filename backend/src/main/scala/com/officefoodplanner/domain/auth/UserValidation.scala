package com.officefoodplanner.domain.auth

import java.time.Instant

import cats.Applicative
import cats.data.EitherT
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.command.UpdateUser
import com.officefoodplanner.domain.auth.model.User
import io.scalaland.chimney.dsl._

trait UserValidation[F[_]] {

  def userMustNotExist(user: Option[User])(implicit AP: Applicative[F]): EitherT[F, UserValidationError, Unit] = {
    val validation = user match {
      case Some(u) => Left(UserAlreadyExistsError(u.username))
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
    val changed =
      newUser
        .into[User]
        .withFieldComputed(_.createdOn, _ => storedUser.createdOn)
        .withFieldComputed(_.active, _ => storedUser.active)
        .withFieldComputed(_.passwordHash, _ => storedUser.passwordHash)
        .withFieldConst(_.updatedOn, Instant.now()) // TODO take current time from environment
        .transform
    val validation = Right[UserValidationError, User](changed)

    EitherT.fromEither[F](validation)
  }

  def checkNewPassword(
      appConfig: ApplicationConfig,
      storedUser: User,
      newPassword: String,
      newHash: String
  )(implicit AP: Applicative[F]): EitherT[F, UserValidationError, User] = {

    val validation =
      if (newPassword.length < appConfig.auth.minimumPasswordLength)
        Left[UserValidationError, User](NewPasswordError("Too short!"))
      else
        Right[UserValidationError, User](storedUser.copy(passwordHash = newHash, updatedOn = Instant.now())) // TODO take time from environment

    EitherT.fromEither[F](validation)
  }

}
