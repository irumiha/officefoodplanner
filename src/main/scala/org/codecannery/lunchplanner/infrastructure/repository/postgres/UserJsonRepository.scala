package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import cats.Monad
import doobie.ConnectionIO
import org.codecannery.lunchplanner.domain.users.UserRepositoryAlgebra
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Table, TableName}

class UserJsonRepository[F[_]: Monad] extends UserRepositoryAlgebra[F] {
  val jsonRepo = new JsonRepository[F, User](table = Table(SchemaName("public"), TableName("user")))

  def create(user: User): F[ConnectionIO[User]] = jsonRepo.create(user)

  def update(user: User): F[ConnectionIO[Int]] = jsonRepo.update(user)

  def get(userId: UUID): F[ConnectionIO[Option[User]]] = jsonRepo.get(userId)

  def delete(userId: UUID): F[ConnectionIO[Int]] = jsonRepo.delete(userId)

  def findByUserName(userName: String): F[ConnectionIO[Option[User]]] = ???

  def deleteByUserName(userName: String): F[ConnectionIO[Int]] = ???

  def list(pageSize: Int, offset: Int): F[ConnectionIO[List[User]]] = ???
}
