package com.officefoodplanner.domain.auth

import java.time.Instant
import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import io.scalaland.chimney.dsl._
import tsec.common.Verified
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.ErrorT
import com.officefoodplanner.domain.auth.command.{CreateUser, UpdateUser, UpdateUserPassword}
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.UserRepository
import com.officefoodplanner.domain.auth.view.UserSimpleView
import com.officefoodplanner.infrastructure.service.TransactingService

abstract class UserService[F[_]: Monad, D[_]: Async, H]
  extends TransactingService[F, D]
    with UserValidation[D]
    with ErrorT[UserValidationError]
{
  val userRepo: UserRepository[D]
  val cryptService: PasswordHasher[D, H]
  val applicationConfig: ApplicationConfig

  def createUser(user: CreateUser): EitherT[F, UserValidationError, User] = {
    val savedAction  = for {
      _            <- userRepo.findByUsername(user.username).errorIfFound(UserAlreadyExistsError(user.username))
      hashedPw     <- cryptService.hashpw(user.password).rightF
      userToCreate  = prepareUserFromCommand(user, hashedPw.toString)
      saved        <- userRepo.create(userToCreate).rightF
    } yield saved

    savedAction.mapK(FunctionK.lift(transact))
  }

  private def prepareUserFromCommand(user: CreateUser, pwhash: String) = {
    val now = Instant.now()
    user.into[User]
      .withFieldConst(_.passwordHash, pwhash)
      .withFieldConst(_.createdOn,    now)
      .withFieldConst(_.updatedOn,    now)
      .transform
  }

  def getUser(userId: UUID): EitherT[F, UserValidationError, User] =
    transact(userRepo.get(userId)).orError(UserNotFoundError)

  def getUserByUsername(username: String): EitherT[F, UserValidationError, User] =
    transact(userRepo.findByUsername(username)).orError(UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] =
    transact(userRepo.deleteById(userId).as(()))

  def update(user: UpdateUser): EitherT[F, UserValidationError, User] =
    (for {
      storedUser   <- userRepo.findByUsername(user.username).orError(UserNotFoundError)
      userToUpdate <- validChanges(storedUser, user)
      _            <- userRepo.update(userToUpdate).rightF
    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def updatePassword(username: String, updatePw: UpdateUserPassword): EitherT[F, UserValidationError, User] =
    (for {
      storedUser       <- userRepo.findByUsername(username).orError(UserNotFoundError)
      oldPasswordCheck <- cryptService.checkpw(updatePw.oldPassword, PasswordHash[H](storedUser.passwordHash)).rightF
      newHash          <- cryptService.hashpw(updatePw.newPassword).rightF
      _                <- validateNewPassword(applicationConfig, updatePw.newPassword).liftError
      userToUpdate     =  storedUser.copy(passwordHash = newHash, updatedOn = Instant.now())
      _                <- Option.when(oldPasswordCheck != Verified)(OldPasswordMismatch).pure[D].liftError
      _                <- userRepo.update(userToUpdate).rightF

    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def list(pageSize: Int, offset: Int): F[List[UserSimpleView]] =
    for {
      dbList <- transact(userRepo.list.map(_.slice(offset, offset + pageSize)))
    } yield {
      dbList.map(u => u.into[UserSimpleView].transform)
    }

}
