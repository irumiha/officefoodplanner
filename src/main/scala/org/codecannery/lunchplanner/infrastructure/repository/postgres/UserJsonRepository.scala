package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie.ConnectionIO
import org.codecannery.lunchplanner.domain.users.UserRepositoryAlgebra
import org.codecannery.lunchplanner.domain.users.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Table, TableName}

class UserJsonRepository extends UserRepositoryAlgebra[ConnectionIO] {
  val jsonRepo = new JsonRepository[User](table = Table(SchemaName("public"), TableName("user"))) {


  }

  def create(user: User): ConnectionIO[User] = jsonRepo.create(user)

  def update(user: User): ConnectionIO[Int] = jsonRepo.update(user)

  def get(userId: UUID): ConnectionIO[Option[User]] = jsonRepo.get(userId)

  def delete(userId: UUID): ConnectionIO[Int] = jsonRepo.delete(userId)

  def findByUserName(userName: String): ConnectionIO[Option[User]] = ???

  def deleteByUserName(userName: String): ConnectionIO[Int] = ???

  def list: ConnectionIO[List[User]] = jsonRepo.list

  def list(pageSize: Int, offset: Int): ConnectionIO[List[User]] = jsonRepo.list(pageSize, offset)

}
