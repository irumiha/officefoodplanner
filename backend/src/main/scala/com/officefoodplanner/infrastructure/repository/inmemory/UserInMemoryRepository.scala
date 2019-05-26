package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import cats.implicits._
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.UserRepository

class UserInMemoryRepository[F[_]: Applicative] extends UserRepository[F] {
  private val repo: InMemoryRepository[F, User, UUID]  = new InMemoryRepository[F, User, UUID]

  override def create(user: User): F[User] = repo.create(user)

  override def update(user: User): F[Int] = repo.update(user)

  override def get(userId: UUID): F[Option[User]] = repo.get(userId)

  override def deleteById(userId: UUID): F[Int] = repo.deleteById(userId)

  override def findByUsername(userName: String): F[Option[User]] =
    repo.listAll.map { l => l.find(_.username == userName)}

  override def deleteByUserName(userName: String): F[Int] =
    repo.delete( _.username == userName).map(_.length)

  override def list: F[List[User]] = repo.listAll
}
