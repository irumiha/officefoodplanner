package org.codecannery.lunchplanner.infrastructure.service.user

import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.syntax.functor._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.scalaland.chimney.dsl._
import org.codecannery.lunchplanner.domain.user._

class UserService[F[_]: Monad](
    userRepo: UserRepository[ConnectionIO],
    validation: UserValidation[ConnectionIO],
    xa: Transactor[F]
) {

  def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)

  def createUser(user: command.CreateUser): EitherT[F, UserAlreadyExistsError, model.User] =
    (for {
      _ <- EitherT(validation.doesNotExist(user.userName))
      userToCreate = user.into[model.User].transform
      saved <- EitherT.liftF[ConnectionIO, UserAlreadyExistsError, model.User](
        userRepo.create(userToCreate))
    } yield saved).mapK(FunctionK.lift(transact))

  def getUser(userId: UUID): EitherT[F, UserNotFoundError.type, model.User] =
    EitherT.fromOptionF(userRepo.get(userId).transact(xa), UserNotFoundError)

  def getUserByUsername(username: String): EitherT[F, UserNotFoundError.type, model.User] =
    EitherT.fromOptionF(userRepo.findByUsername(username).transact(xa), UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] =
    userRepo.deleteById(userId).as(()).transact(xa)

  def deleteByUsername(username: String): F[Unit] =
    userRepo.deleteByUserName(username).as(()).transact(xa)

  def update(user: command.UpdateUser): EitherT[F, UserValidationError, model.User] =
    (for {
      storedUser <- EitherT(validation.exists(user.key))
      userToUpdate <- EitherT.fromEither[ConnectionIO](validation.validChanges(storedUser, user))
      updatedUser <- persistUpdatedUser(userToUpdate)
    } yield updatedUser).mapK(FunctionK.lift(transact))

  private def persistUpdatedUser(user: model.User): EitherT[ConnectionIO, UserValidationError, model.User] = {
    val updated = userRepo.update(user)
    val of = Monad[ConnectionIO].map(updated) { u =>
      if (u == 1) Some(user) else None
    }
    EitherT.fromOptionF(of, UserNotFoundError)
  }

  def list(pageSize: Int, offset: Int): F[List[view.UserListView]] =
    for {
      dbList <- userRepo.list(pageSize, offset).transact(xa)
    } yield {
      dbList.map(u => u.into[view.UserListView].transform)
    }

}

object UserService {
  def apply[F[_]: Monad](
      repository: UserRepository[ConnectionIO],
      validation: UserValidation[ConnectionIO],
      xa: Transactor[F]
  ): UserService[F] =
    new UserService[F](repository, validation, xa)
}
