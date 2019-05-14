package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.UserRepository
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.infrastructure.repository.{DoobieColumn, DoobieSupport, SchemaName, Table, TableName}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

class UserTableRepository extends UserRepository[ConnectionIO] {

  import UserTableRepository.doobieSupport

  private val repo: TableRepository[User, UUID] = new TableRepository[User, UUID] {
    override val table = Table(SchemaName("auth"), TableName("users"))
  }

  private def byUsername(userName: String) = fr"username = $userName"

  def create(user: User): ConnectionIO[User] = repo.create(user)

  def update(user: User): ConnectionIO[Int] = repo.update(user)

  def get(userId: UUID): ConnectionIO[Option[User]] = repo.get(userId)

  def deleteById(userId: UUID): ConnectionIO[Int] = repo.deleteById(userId)

  def list: ConnectionIO[List[User]] = repo.listAll

  def findByUsername(userName: String): ConnectionIO[Option[User]] =
    repo.find(byUsername(userName), None, None, None).map(_.headOption)

  def deleteByUserName(userName: String): ConnectionIO[Int] =
    repo.delete(byUsername(userName)).map(_.size)
}

object UserTableRepository {
  implicit val doobieSupport: DoobieSupport[User] = new DoobieSupport[User] {
    override val id: DoobieColumn[User] = DoobieColumn[User]("id", u => fr0"${u.id}")

    override val columns: List[DoobieColumn[User]] = List(
      DoobieColumn[User]("id",          e => fr0"${e.id}"),
      DoobieColumn[User]("username",    e => fr0"${e.userName}"),
      DoobieColumn[User]("first_name",  e => fr0"${e.firstName}"),
      DoobieColumn[User]("last_name",   e => fr0"${e.lastName}"),
      DoobieColumn[User]("email",       e => fr0"${e.email}"),
      DoobieColumn[User]("hash",        e => fr0"${e.hash}"),
      DoobieColumn[User]("phone",       e => fr0"${e.phone}"),
      DoobieColumn[User]("initialized", e => fr0"${e.initialized}"),
      DoobieColumn[User]("created_on",  e => fr0"${e.createdOn}"),
      DoobieColumn[User]("updated_on",  e => fr0"${e.updatedOn}"),
    )
  }

}
