package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import cats.implicits._
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.UserRepository

class UserInMemoryRepository[F[_]: Applicative] extends UserRepository[F] {
  private val dao: InMemoryDao.Aux[F, User, UUID] =
    InMemoryDao[F, User](InMemoryDao.derive[F, User, UUID].apply(_.id))

  override def create(user: User): F[User] = dao.create(user)

  override def update(user: User): F[Int] = dao.update(user)

  override def get(userId: UUID): F[Option[User]] = dao.get(userId)

  override def deleteById(userId: UUID): F[Int] = dao.deleteById(userId)

  override def findByUsername(userName: String): F[Option[User]] =
    dao.listAll.map { l => l.find(_.username == userName)}

  override def list: F[List[User]] = dao.listAll
}
