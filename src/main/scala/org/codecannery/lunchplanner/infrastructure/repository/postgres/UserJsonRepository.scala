package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie.ConnectionIO
import doobie.syntax.string._
import org.codecannery.lunchplanner.domain.user.UserRepository
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Specification, Table, TableName}

class UserJsonRepository extends UserRepository[ConnectionIO] {
  private val repo: JsonRepository[User] = new JsonRepository[User] {
    override val table = Table(SchemaName("public"), TableName("users"))
  }

  private def byUsername(userName: String) = fr"where data->>'userName' = $userName"

  def create(user: User): ConnectionIO[User] = repo.create(user)

  def update(user: User): ConnectionIO[Int] = repo.update(user)

  def get(userId: UUID):  ConnectionIO[Option[User]] = repo.get(userId)

  def deleteById(userId: UUID): ConnectionIO[Int] = repo.deleteById(userId)

  def list: ConnectionIO[List[User]] = repo.list

  def list(pageSize: Int, offset: Int): ConnectionIO[List[User]] = repo.list(pageSize, offset)

  def findByUsername(userName: String): ConnectionIO[Option[User]] =
    repo.find(Specification(byUsername(userName))).option

  def deleteByUserName(userName: String): ConnectionIO[Int] = {
    repo.delete(Specification(byUsername(userName))).to[List].map(_.size)
  }

}
