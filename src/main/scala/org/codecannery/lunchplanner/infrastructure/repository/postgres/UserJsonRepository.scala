package org.codecannery.lunchplanner.infrastructure.repository.postgres

import doobie.ConnectionIO
import doobie.syntax.string._
import org.codecannery.lunchplanner.domain.user.UserRepositoryAlgebra
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{SchemaName, Specification, Table, TableName}

class UserJsonRepository extends JsonRepository[User] with UserRepositoryAlgebra[ConnectionIO] {
  override val table = Table(SchemaName("public"), TableName("users"))

  def findByUsername(userName: String): ConnectionIO[Option[User]] =
    find(Specification(fr"where data->>'userName' = $userName")).option

  def deleteByUserName(userName: String): ConnectionIO[Int] = {
    delete(Specification(fr"""where data->>'userName' = $userName""")).to[List].map(_.size)
  }

}
