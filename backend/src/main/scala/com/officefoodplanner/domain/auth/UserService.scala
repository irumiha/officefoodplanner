package com.officefoodplanner.domain.auth

import java.time.Instant
import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.command.{CreateUser, UpdateUser, UpdateUserPassword}
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.UserRepository
import com.officefoodplanner.domain.auth.view.UserSimpleView
import com.officefoodplanner.infrastructure.service.TransactingService
import io.scalaland.chimney.dsl._
import tsec.common.Verified
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

abstract class UserService[F[_]: Monad, D[_]: Async, H] extends TransactingService[F, D] with UserValidation[D] {
  val userRepo: UserRepository[D]
  val cryptService: PasswordHasher[D, H]
  val applicationConfig: ApplicationConfig

  def createUser(user: CreateUser): EitherT[F, UserValidationError, User] = {
    val savedAction  = for {
      maybeUser <- getUser(user)
      _         <- userMustNotExist(maybeUser)
      hashedPw  <- EitherT.right(cryptService.hashpw(user.password))
      userToCreate = prepareUserFromCommand(user, hashedPw.toString)
      saved     <- createUser(userToCreate)
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

  private def createUser(userToCreate: User) =
    EitherT.liftF[D, UserValidationError, User](userRepo.create(userToCreate))

  private def getUser(user: CreateUser): EitherT[D, UserValidationError, Option[User]] =
    EitherT.right(userRepo.findByUsername(user.username))

  def getUser(userId: UUID): EitherT[F, UserValidationError, User] =
    EitherT.fromOptionF(transact(userRepo.get(userId)), UserNotFoundError)

  def getUserByUsername(username: String): EitherT[F, UserValidationError, User] =
    EitherT.fromOptionF(transact(userRepo.findByUsername(username)), UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] =
    transact(userRepo.deleteById(userId).as(()))

  def deleteByUsername(username: String): F[Unit] =
    transact(userRepo.deleteByUserName(username).as(()))

  def update(user: UpdateUser): EitherT[F, UserValidationError, User] =
    (for {
      maybeUser <- EitherT.right(userRepo.findByUsername(user.username))
      storedUser <- userMustExist(maybeUser)
      userToUpdate <- validChanges(storedUser, user)
      _ <- EitherT.liftF[D, UserValidationError, Int](userRepo.update(userToUpdate))
    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def updatePassword(username: String, updatePw: UpdateUserPassword): EitherT[F, UserValidationError, User] =
    (for {
      maybeUser    <- EitherT.right(userRepo.findByUsername(username))
      storedUser   <- userMustExist(maybeUser)
      oldPasswordCheck <- EitherT.liftF(
        cryptService.checkpw(updatePw.oldPassword, PasswordHash[H](storedUser.passwordHash))
      )
      newHash      <- EitherT.right(cryptService.hashpw(updatePw.newPassword))
      userToUpdate <-
        if (oldPasswordCheck == Verified)
          checkNewPassword(applicationConfig, storedUser, updatePw.newPassword, newHash.toString)
        else
          EitherT.leftT[D, User](OldPasswordMismatch)
      _ <- EitherT.liftF[D, UserValidationError, Int](userRepo.update(userToUpdate))
    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def list(pageSize: Int, offset: Int): F[List[UserSimpleView]] =
    for {
      dbList <- transact(userRepo.list.map(_.slice(offset, offset + pageSize)))
    } yield {
      dbList.map(u => u.into[UserSimpleView].transform)
    }

}
