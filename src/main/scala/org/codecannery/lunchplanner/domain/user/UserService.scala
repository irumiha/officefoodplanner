package org.codecannery.lunchplanner.domain.user

import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.syntax.functor._
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.infrastructure.service.TransactingService

abstract class UserService[F[_]: Monad, D[_]: Monad](
    userRepo: UserRepository[D],
    validation: UserValidation[D]
) extends TransactingService[F, D] {

  def createUser(user: command.CreateUser): EitherT[F, UserAlreadyExistsError, model.User] =
    (for {
      _ <- EitherT(validation.doesNotExist(user.userName))
      userToCreate = user.into[model.User].transform
      saved <- EitherT.liftF[D, UserAlreadyExistsError, model.User](
        userRepo.create(userToCreate))
    } yield saved).mapK(FunctionK.lift(transact))

  def getUser(userId: UUID): EitherT[F, UserNotFoundError.type, model.User] =
    EitherT.fromOptionF(transact(userRepo.get(userId)), UserNotFoundError)

  def getUserByUsername(username: String): EitherT[F, UserNotFoundError.type, model.User] =
    EitherT.fromOptionF(transact(userRepo.findByUsername(username)), UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] =
    transact(userRepo.deleteById(userId).as(()))

  def deleteByUsername(username: String): F[Unit] =
    transact(userRepo.deleteByUserName(username).as(()))

  def update(user: command.UpdateUser): EitherT[F, UserValidationError, model.User] =
    (for {
      storedUser <- EitherT(validation.exists(user.key))
      userToUpdate <- EitherT.fromEither[D](validation.validChanges(storedUser, user))
      updatedUser <- persistUpdatedUser(userToUpdate)
    } yield updatedUser).mapK(FunctionK.lift(transact))

  private def persistUpdatedUser(user: model.User): EitherT[D, UserValidationError, model.User] = {
    val updated = userRepo.update(user)
    val of = Monad[D].map(updated) { u =>
      if (u == 1) Some(user) else None
    }
    EitherT.fromOptionF(of, UserNotFoundError)
  }

  def list(pageSize: Int, offset: Int): F[List[view.UserListView]] =
    for {
      dbList <- transact(userRepo.list(pageSize, offset))
    } yield {
      dbList.map(u => u.into[view.UserListView].transform)
    }

}
