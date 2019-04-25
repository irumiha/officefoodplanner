package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie.ConnectionIO
import doobie.syntax.string._
import doobie.util.fragment.Fragment
import org.codecannery.lunchplanner.domain.user.UserRepository
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Table, TableName}

class UserJsonRepository extends UserRepository[ConnectionIO] {
  private val repo: JsonRepository[User] = new JsonRepository[User] {
    override val table = Table(SchemaName("public"), TableName("users"))
  }

  private def byUsername(userName: String) = fr"data->>'userName' = $userName"

  def create(user: User): ConnectionIO[User] = repo.create(user)

  def update(user: User): ConnectionIO[Int] = repo.update(user)

  def get(userId: UUID):  ConnectionIO[Option[User]] = repo.get(userId)

  def deleteById(userId: UUID): ConnectionIO[Int] = repo.deleteById(userId)

  def list: ConnectionIO[List[User]] = repo.listAll

  def findByUsername(userName: String): ConnectionIO[Option[User]] =
    repo.find(byUsername(userName), None, None, None).map(_.headOption)

  def deleteByUserName(userName: String): ConnectionIO[Int] = {
    repo.delete(byUsername(userName)).map(_.size)
  }

}
