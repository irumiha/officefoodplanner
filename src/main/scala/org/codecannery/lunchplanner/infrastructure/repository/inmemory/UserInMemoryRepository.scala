package org.codecannery.lunchplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats.Applicative
import cats.implicits._
import org.codecannery.lunchplanner.domain.users.UserRepositoryAlgebra
import org.codecannery.lunchplanner.domain.users.model.User

class UserInMemoryRepository[F[_]: Applicative]
    extends InMemoryRepository[F, User, UUID]
    with UserRepositoryAlgebra[F] {
  override def entityKey(e: User): UUID = e.key

  override def findByUserName(userName: String): F[Option[User]] =
    cache.values.find(_.userName == userName).pure[F]

  override def deleteByUserName(userName: String): F[Int] = {
    val existing = cache.values.find(_.userName == userName)
    existing.foreach { e =>
      cache.remove(e.key)
    }
    existing.size.toInt
  }.pure[F]


  override def list: F[List[User]] = cache.values.toList.pure[F]

  override def list(pageSize: Int, offset: Int): F[List[User]] =
    cache.values.toList.slice(offset, offset + pageSize).pure[F]
}
