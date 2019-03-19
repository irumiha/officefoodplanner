package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie.free.connection.ConnectionIO
import org.codecannery.lunchplanner.domain.users.UserRepositoryAlgebra
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Table, TableName}

class UserJsonRepository extends UserRepositoryAlgebra[F] {
  val jsonRepo = new JsonRepository[ConnectionIO, User](table = Table(SchemaName("public"), TableName("user")))

  override def create(user: User): ConnectionIO[User] = jsonRepo.create(user)

  override def update(user: User): ConnectionIO[Int] = jsonRepo.update(user)

  override def get(userId: UUID): ConnectionIO[Option[User]] = jsonRepo.get(userId)

  override def delete(userId: UUID): ConnectionIO[Int] = jsonRepo.delete(userId)

  override def findByUserName(userName: String): ConnectionIO[Option[User]] = ???

  override def deleteByUserName(userName: String): ConnectionIO[Int] = ???

  override def list(pageSize: Int, offset: Int): ConnectionIO[List[User]] = ???
}
