package org.codecannery.lunchplanner.domain.users

import java.util.UUID

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.syntax.functor._
import io.bfil.automapper._
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._
import org.codecannery.lunchplanner.domain.users.command.{CreateUser, UpdateUser}
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.users.view.UserListView
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

class UserService[F[_]: Monad](
    userRepo: UserRepositoryAlgebra[ConnectionIO],
    validation: UserValidationAlgebra[ConnectionIO],
    xa: Transactor[F]
) {

  def transact[A](t: ConnectionIO[A]): F[A]  = t.transact(xa)

  def createUser(user: CreateUser): EitherT[F, UserAlreadyExistsError, User] = {
    val transaction = for {
      _            <- EitherT(validation.doesNotExist(user.userName))
      userToCreate = automap(user).to[User]
      saved        <- EitherT.liftF(userRepo.create(userToCreate))
    } yield saved


    transaction.mapK(FunctionK.lift(transact))
  }

  def getUser(userId: UUID): EitherT[F, UserNotFoundError.type, User] =
    EitherT.fromOptionF(userRepo.get(userId).transact(xa), UserNotFoundError)

  def getUserByName(userName: String): EitherT[F, UserNotFoundError.type, User] =
    EitherT.fromOptionF(userRepo.findByUserName(userName).transact(xa), UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] = userRepo.delete(userId).as(()).transact(xa)

  def deleteByUserName(userName: String): F[Unit] =
    userRepo.deleteByUserName(userName).as(()).transact(xa)

  def update(user: UpdateUser): EitherT[F, UserValidationError, User] =
    (for {
      storedUser <- EitherT(validation.exists(user.key))
      userToUpdate <- EitherT(validation.validChanges(storedUser, user))
      updatedUser <- persistUpdatedUser(userToUpdate)
    } yield updatedUser)

  private def persistUpdatedUser(user: User): EitherT[ConnectionIO, UserValidationError, User] = {
    val updated = userRepo.update(user)
    val of = Monad[ConnectionIO].map(updated) { u =>
      if (u == 1) Some(user) else None
    }
    EitherT.fromOptionF(of, UserNotFoundError)
  }

  def list(pageSize: Int, offset: Int): F[List[UserListView]] =
    for {
      dbList <- userRepo.list(pageSize, offset).transact(xa)
    } yield {
      dbList.map(u => automap(u).to[UserListView])
    }

}



object UserService {
  def apply[F[_]: Monad](
      repository: UserRepositoryAlgebra[ConnectionIO],
      validation: UserValidationAlgebra[ConnectionIO],
      xa: Transactor[F]
  ): UserService[F] =
    new UserService[F](repository, validation, xa)
}
