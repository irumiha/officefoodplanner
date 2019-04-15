package org.codecannery.lunchplanner.domain.user

import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.implicits._
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.domain.user.command.CreateUser
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.service.TransactingService

abstract class UserService[F[_]: Monad, D[_]: Monad] extends TransactingService[F, D] with UserValidation[D] {
  val userRepo: UserRepository[D]

  def createUser(user: command.CreateUser): EitherT[F, UserValidationError, model.User] =
    (for {
      maybeUser <- getUser(user)
      _ <- userMustNotExist(maybeUser)
      userToCreate = prepareUserFromCommand(user)
      saved <- createUser(userToCreate)
    } yield saved).mapK(FunctionK.lift(transact))

  private def prepareUserFromCommand(user: CreateUser) =
    user.into[User].transform

  private def createUser(userToCreate: User) =
    EitherT.liftF[D, UserValidationError, User](userRepo.create(userToCreate))

  private def getUser(user: CreateUser): EitherT[D, Nothing, Option[User]] =
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
      _ <- EitherT.right(userRepo.update(userToUpdate))
    } yield userToUpdate).mapK(FunctionK.lift(transact))

  def list(pageSize: Int, offset: Int): F[List[view.UserListView]] =
    for {
      dbList <- transact(userRepo.list(pageSize, offset))
    } yield {
      dbList.map(u => u.into[view.UserListView].transform)
    }

}
