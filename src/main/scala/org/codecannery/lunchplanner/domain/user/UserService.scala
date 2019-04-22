package org.codecannery.lunchplanner.domain.user

import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.implicits._
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.infrastructure.service.TransactingService
import tsec.passwordhashers.PasswordHasher

abstract class UserService[F[_]: Monad, D[_]: Monad, H] extends TransactingService[F, D] with UserValidation[D] {
  val userRepo: UserRepository[D]
  val cryptService: PasswordHasher[D, H]

  def createUser(user: command.CreateUser): EitherT[F, UserValidationError, model.User] =
    (for {
      maybeUser <- getUser(user)
      _ <- userMustNotExist(maybeUser)
      hashedPw <- EitherT.right(cryptService.hashpw(user.password))
      userToCreate = prepareUserFromCommand(user, hashedPw.toString)
      saved <- createUser(userToCreate)
    } yield saved).mapK(FunctionK.lift(transact))

  private def prepareUserFromCommand(user: command.CreateUser, pwhash: String) =
    user.into[model.User]
      .withFieldComputed(_.hash, _ => pwhash)
      .transform

  private def createUser(userToCreate: model.User) =
    EitherT.liftF[D, UserValidationError, model.User](userRepo.create(userToCreate))

  private def getUser(user: command.CreateUser): EitherT[D, UserValidationError, Option[model.User]] =
    EitherT.right(userRepo.findByUsername(user.userName))

  def getUser(userId: UUID): EitherT[F, UserValidationError, model.User] =
    EitherT.fromOptionF(transact(userRepo.get(userId)), UserNotFoundError)

  def getUserByUsername(username: String): EitherT[F, UserValidationError, model.User] =
    EitherT.fromOptionF(transact(userRepo.findByUsername(username)), UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] =
    transact(userRepo.deleteById(userId).as(()))

  def deleteByUsername(username: String): F[Unit] =
    transact(userRepo.deleteByUserName(username).as(()))

  def update(user: command.UpdateUser): EitherT[F, UserValidationError, model.User] =
    (for {
      maybeUser <- EitherT.right(userRepo.findByUsername(user.userName))
      storedUser <- userMustExist(maybeUser)
      userToUpdate <- validChanges(storedUser, user)
      _ <- EitherT.liftF[D, UserValidationError, Int](userRepo.update(userToUpdate))
    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def list(pageSize: Int, offset: Int): F[List[view.UserListView]] =
    for {
      dbList <- transact(userRepo.list.map(_.slice(offset, offset + pageSize)))
    } yield {
      dbList.map(u => u.into[view.UserListView].transform)
    }

}
