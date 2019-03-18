package org.codecannery.lunchplanner.infrastructure.repository.inmemory

import java.util.{Random, UUID}

import cats.Applicative
import cats.implicits._
import io.bfil.automapper.automap
import org.codecannery.lunchplanner.domain.users.UserRepositoryAlgebra
import org.codecannery.lunchplanner.domain.users.command.CreateUser
import org.codecannery.lunchplanner.domain.users.command.UpdateUser
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.domain.users.view.UserListView

import scala.collection.concurrent.TrieMap

class UserRepositoryInMemoryInterpreter[F[_]: Applicative] extends UserRepositoryAlgebra[F] {

  private val cache = new TrieMap[UUID, User]

  private val random = new Random

  def create(user: User): F[User] = {
    cache += (user.key -> user)
    user.pure[F]
  }

  def update(user: User): F[Option[User]] = cache.get(user.key).traverse { u =>
    cache.update(user.key, user)
    user.pure[F]
  }

  def get(id: UUID): F[Option[User]] = cache.get(id).pure[F]

  def delete(id: UUID): F[Option[User]] = cache.remove(id).pure[F]

  def findByUserName(userName: String): F[Option[User]] =
    cache.values.find(u => u.userName == userName).pure[F]

  def list(pageSize: Int, offset: Int): F[List[User]] = {
    cache.values.toList.sortBy(_.lastName).slice(offset, offset + pageSize).pure[F]
  }

  def deleteByUserName(userName: String): F[Option[User]] = {
    val deleted = for {
      user <- cache.values.find(u => u.userName == userName)
      removed <- cache.remove(user.key)
    } yield removed
    deleted.pure[F]
  }
}

object UserRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative]() = new UserRepositoryInMemoryInterpreter[F]
}
