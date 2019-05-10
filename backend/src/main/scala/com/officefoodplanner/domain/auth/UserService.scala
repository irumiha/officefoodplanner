package com.officefoodplanner.domain.auth

import java.time.Instant
import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.effect.Async
import cats.implicits._
import com.officefoodplanner.domain.auth.command.{CreateUser, UpdateUser}
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.view.UserSimpleView
import com.officefoodplanner.infrastructure.service.TransactingService
import io.scalaland.chimney.dsl._
import tsec.passwordhashers.PasswordHasher

abstract class UserService[F[_]: Monad, D[_]: Async, H] extends TransactingService[F, D] with UserValidation[D] {
  val userRepo: UserRepository[D]
  val cryptService: PasswordHasher[D, H]

  def createUser(user: CreateUser): EitherT[F, UserValidationError, User] = {
    val savedAction  = for {
      maybeUser <- getUser(user)
      _ <- userMustNotExist(maybeUser)
      hashedPw <- EitherT.right(cryptService.hashpw(user.password))
      userToCreate = prepareUserFromCommand(user, hashedPw.toString)
      saved <- createUser(userToCreate)
    } yield saved

    savedAction.mapK(FunctionK.lift(transact))
  }

  private def prepareUserFromCommand(user: CreateUser, pwhash: String) = {
    val now = Instant.now()
    user.into[User]
      .withFieldComputed(_.hash, _ => pwhash)
      .withFieldComputed(_.createdOn, _ => now)
      .withFieldComputed(_.updatedOn, _ => now)
      .transform
  }

  private def createUser(userToCreate: User) =
    EitherT.liftF[D, UserValidationError, User](userRepo.create(userToCreate))

  private def getUser(user: CreateUser): EitherT[D, UserValidationError, Option[User]] =
    EitherT.right(userRepo.findByUsername(user.userName))

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
      maybeUser <- EitherT.right(userRepo.findByUsername(user.userName))
      storedUser <- userMustExist(maybeUser)
      userToUpdate <- validChanges(storedUser, user)
      _ <- EitherT.liftF[D, UserValidationError, Int](userRepo.update(userToUpdate))
    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def list(pageSize: Int, offset: Int): F[List[UserSimpleView]] =
    for {
      dbList <- transact(userRepo.list.map(_.slice(offset, offset + pageSize)))
    } yield {
      dbList.map(u => u.into[UserSimpleView].transform)
    }

}
