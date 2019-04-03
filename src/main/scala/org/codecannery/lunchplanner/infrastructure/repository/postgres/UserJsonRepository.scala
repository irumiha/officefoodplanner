package org.codecannery.lunchplanner.infrastructure.repository.postgres

import doobie.ConnectionIO
import doobie.syntax.string._
import org.codecannery.lunchplanner.domain.user.UserRepository
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Specification, Table, TableName}

class UserJsonRepository extends JsonRepository[User] with UserRepository[ConnectionIO] {
    override val table = Table(SchemaName("public"), TableName("users"))

  private def byUsername(userName: String) = fr"where data->>'userName' = $userName"

  def findByUsername(userName: String): ConnectionIO[Option[User]] =
    find(Specification(byUsername(userName))).option

  def deleteByUserName(userName: String): ConnectionIO[Int] = {
    delete(Specification(byUsername(userName))).to[List].map(_.size)
  }

}
