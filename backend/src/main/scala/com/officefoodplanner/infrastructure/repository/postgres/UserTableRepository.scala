package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.UserRepository
import com.officefoodplanner.infrastructure.repository.{SchemaName, Table, TableName}
import doobie._
import doobie.postgres.implicits._
import doobie.implicits.legacy.instant._

object UserTableRepository extends UserRepository[ConnectionIO] {
  private val table = Table(SchemaName("auth"), TableName("users"))

  private val dao =
    TableDao[User](TableDao.derive[User, UUID](_.id, "id", table))

  def create(user: User): ConnectionIO[User] = dao.create(user)

  def update(user: User): ConnectionIO[Int] = dao.update(user)

  def get(userId: UUID): ConnectionIO[Option[User]] = dao.get(userId)

  def deleteById(userId: UUID): ConnectionIO[Int] = dao.deleteById(userId)

  def list: ConnectionIO[List[User]] = dao.listAll

  def findByUsername(userName: String): ConnectionIO[Option[User]] = {
    Query[String, User](
      s"select ${dao.columnsQuoted} from $table where username = ?"
    ).option(userName)
  }

}
