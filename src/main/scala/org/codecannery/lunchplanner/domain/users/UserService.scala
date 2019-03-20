package org.codecannery.lunchplanner.domain.users

import java.util.UUID

import cats._
import cats.data._
import cats.effect.IO
import cats.syntax.functor._
import io.bfil.automapper._
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.codecannery.lunchplanner.domain.users.command.{CreateUser, UpdateUser}
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.users.view.UserListView
import org.codecannery.lunchplanner.domain.{UserAlreadyExistsError, UserNotFoundError, UserValidationError}

class UserService[F[_]: Monad](
    userRepo: UserRepositoryAlgebra[F],
    validation: UserValidationAlgebra[F]) {

  def createUser(user: CreateUser): EitherT[F, UserAlreadyExistsError, User] =
    for {
      _ <- EitherT(validation.doesNotExist(user.userName))
      userToCreate = automap(user).to[User]
      saved <- EitherT.liftF(userRepo.create(userToCreate))
    } yield saved

  def getUser(userId: UUID): EitherT[F, UserNotFoundError.type, User] =
    EitherT.fromOptionF(userRepo.get(userId), UserNotFoundError)

  def getUserByName(userName: String): EitherT[F, UserNotFoundError.type, User] =
    EitherT.fromOptionF(userRepo.findByUserName(userName), UserNotFoundError)

  def deleteUser(userId: UUID): F[Unit] = userRepo.delete(userId).as(())

  def deleteByUserName(userName: String): F[Unit] =
    userRepo.deleteByUserName(userName).as(())

  def update(user: UpdateUser): EitherT[F, UserValidationError, User] =
    for {
      storedUser <- EitherT(validation.exists(user.key))
      userToUpdate <- EitherT(validation.validChanges(storedUser, user))
      updatedUser <- persistUpdatedUser(userToUpdate)
    } yield updatedUser

  private def persistUpdatedUser(user: User): EitherT[F, UserValidationError, User] = {
    val updated = userRepo.update(user)
    val of = Monad[F].map(updated) { u =>
      if (u == 1) Some(user) else None
    }
    EitherT.fromOptionF(of, UserNotFoundError)
  }

  def list(pageSize: Int, offset: Int): F[List[UserListView]] =
    for {
      dbList <- userRepo.list(pageSize, offset)
    } yield {
      dbList.map(u => automap(u).to[UserListView])
    }

}

class DoobieUserService[F[_]: Monad](
    repository: UserRepositoryAlgebra[ConnectionIO],
    validation: UserValidationAlgebra[ConnectionIO],
    xa: Transactor[F]
) extends UserService[ConnectionIO](repository, validation) {
  import doobie.implicits._
  import cats.implicits._
  import cats._
  override def createUser(user: CreateUser): EitherT[F, UserAlreadyExistsError, User] = EitherT {
    val fk:ConnectionIO ~> F = λ[ConnectionIO ~> F](_.transact(xa))

    super.createUser(user).mapK(fk)
  }


  override def getUser(userId: UUID): EitherT[F, UserNotFoundError.type, User] =
    super.getUser(userId)

  override def getUserByName(userName: String): EitherT[F, UserNotFoundError.type, User] =
    super.getUserByName(userName)

  override def deleteUser(userId: UUID): F[Unit] = super.deleteUser(userId)

  override def deleteByUserName(userName: String): F[Unit] = super.deleteByUserName(userName)

  override def update(user: UpdateUser): EitherT[F, UserValidationError, User] = super.update(user)

  override def list(pageSize: Int, offset: Int): F[List[UserListView]] =
    super.list(pageSize, offset)
}

class InMemUserService[F[_]: Monad](
    repository: UserRepositoryAlgebra[IO],
    validation: UserValidationAlgebra[IO]
) extends UserService[IO](repository, validation)

object UserService {
  def withDoobie[F[_]: Monad](
      repository: UserRepositoryAlgebra[ConnectionIO],
      validation: UserValidationAlgebra[ConnectionIO]): UserService[ConnectionIO] =
    new DoobieUserService[F](repository, validation)

  def withInMem[F[_]: Monad](
      repository: UserRepositoryAlgebra[IO],
      validation: UserValidationAlgebra[IO]): UserService[IO] =
    new InMemUserService[F](repository, validation)
}
