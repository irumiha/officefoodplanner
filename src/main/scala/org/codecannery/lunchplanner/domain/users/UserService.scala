package org.codecannery.lunchplanner.domain.users

import cats._
import cats.data._
import cats.syntax.functor._
import io.bfil.automapper._

import org.codecannery.lunchplanner.domain.users.command.{CreateUser, UpdateUser}
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.users.view.UserListView
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

class UserService[F[_]: Monad](userRepo: UserRepositoryAlgebra[F], validation: UserValidationAlgebra[F]) {

  def createUser(user: CreateUser): EitherT[F, UserAlreadyExistsError, User] =
    for {
      _ <- validation.doesNotExist(user.userName)
      userToCreate = automap(user).to[User]
      saved <- EitherT.liftF(userRepo.create(userToCreate))
    } yield saved

  def getUser(userId: Long): EitherT[F, UserNotFoundError.type, User] =
    EitherT.fromOptionF(userRepo.get(userId), UserNotFoundError)

  def getUserByName(userName: String): EitherT[F, UserNotFoundError.type, User] =
    EitherT.fromOptionF(userRepo.findByUserName(userName), UserNotFoundError)

  def deleteUser(userId: Long): F[Unit] = userRepo.delete(userId).as(())

  def deleteByUserName(userName: String): F[Unit] =
    userRepo.deleteByUserName(userName).as(())

  def update(user: UpdateUser): EitherT[F, UserValidationError, User] =
    for {
      storedUser   <- validation.exists(user.id)
      updatedUser  <- validation.validChanges(storedUser, user)
      saved        <- EitherT.fromOptionF(userRepo.update(updatedUser), UserNotFoundError: UserValidationError)
    } yield saved

  def list(pageSize: Int, offset: Int): F[List[UserListView]] =
    for {
      dbList <- userRepo.list(pageSize, offset)
    } yield {
      dbList.map(u => automap(u).to[UserListView])
    }

}

object UserService {
  def apply[F[_]: Monad](repository: UserRepositoryAlgebra[F], validation: UserValidationAlgebra[F]): UserService[F] =
    new UserService[F](repository, validation)
}
