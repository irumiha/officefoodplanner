package com.officefoodplanner.domain.auth

import cats.MonadError
import cats.data.OptionT
import cats.effect.Sync
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

import java.time.Instant
import java.util.UUID

abstract class UserService[F[_]: MonadError[*[_], Throwable], D[_]: Sync, H] extends TransactingService[F, D] with UserValidation[D] {
  val userRepo: UserRepository[D]
  val cryptService: PasswordHasher[D, H]
  val applicationConfig: ApplicationConfig

  def createUser(user: CreateUser): F[User] = {
    def checkUserDoesNotExist =
      userRepo
        .findByUsername(user.username)
        .ensure(UserAlreadyExistsError(user.username))(_.isEmpty)

    val savedAction: D[User] = for {
      _           <- checkUserDoesNotExist
      hashedPw    <- cryptService.hashpw(user.password)
      userToCreate = prepareUserFromCommand(user, hashedPw)
      saved       <- userRepo.create(userToCreate)
    } yield saved

    transact(savedAction)
  }

  private def prepareUserFromCommand(user: CreateUser, pwhash: String) = {
    val now = Instant.now()
    user
      .into[User]
      .withFieldConst(_.passwordHash, pwhash)
      .withFieldConst(_.createdOn, now)
      .withFieldConst(_.updatedOn, now)
      .transform
  }

  def getUser(userId: UUID): F[Option[User]] =
    transact(userRepo.get(userId))

  def getUserByUsername(username: String): F[Option[User]] =
    transact(userRepo.findByUsername(username))

  def deleteUser(userId: UUID): F[Unit] =
    transact(userRepo.deleteById(userId).as(()))

  def update(user: UpdateUser): F[User] =
    transact(for {
      storedUser   <- userRepo.findByUsername(user.username).ensure(UserNotFoundError)(_.isDefined)
      userToUpdate <- validChanges(storedUser.get, user)
      _            <- userRepo.update(userToUpdate)
    } yield userToUpdate)

  def updatePassword(username: String, updatePw: UpdateUserPassword): F[User] =
    transact(for {
      storedUserMaybe  <- userRepo.findByUsername(username)
      storedUser       <- if (storedUserMaybe.isDefined) D.pure(storedUserMaybe.get) else D.raiseError(UserNotFoundError)
      oldPasswordCheck <- cryptService.checkpw(updatePw.oldPassword, PasswordHash[H](storedUser.passwordHash))
      newHash          <- cryptService.hashpw(updatePw.newPassword)
      _                <- raiseIfPasswordError(updatePw, storedUser)
      userToUpdate      = storedUser.copy(passwordHash = newHash, updatedOn = Instant.now())
      _                <- D.pure(oldPasswordCheck == Verified).ensure(OldPasswordMismatch)(m => m)
      _                <- userRepo.update(userToUpdate)

    } yield userToUpdate)

  private def raiseIfPasswordError(updatePw: UpdateUserPassword, storedUser: User) = {
    validateNewPassword(applicationConfig, updatePw.newPassword) match {
      case Some(error) => D.raiseError[User](error)
      case _ => D.pure(storedUser)
    }
  }

  def list(pageSize: Int, offset: Int): F[List[UserSimpleView]] =
    for {
      dbList <- transact(userRepo.list.map(_.slice(offset, offset + pageSize)))
    } yield {
      dbList.map(u => u.into[UserSimpleView].transform)
    }

}
