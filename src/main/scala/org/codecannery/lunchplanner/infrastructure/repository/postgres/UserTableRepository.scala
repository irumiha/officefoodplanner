package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.codecannery.lunchplanner.domain.user.UserRepository
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.repository.{DoobieSupport, SchemaName, Table, TableName}

class UserTableRepository extends UserRepository[ConnectionIO] {
  import UserTableRepository.doobieSupport

  private val repo: TableRepository[User, UUID] = new TableRepository[User, UUID] {
    override val table = Table(SchemaName("public"), TableName("users"))
  }

  private def byUsername(userName: String) = fr"data->>'userName' = $userName"

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
    override def id: String = "key"

    override def values(e: User): List[Fragment] = List(
      fr0"${e.key}",
      fr0"${e.userName}",
      fr0"${e.firstName}",
      fr0"${e.lastName}",
      fr0"${e.email}",
      fr0"${e.hash}",
      fr0"${e.phone}",
    )

    override def columns: List[String] = List(
      "key",
      "userName",
      "firstName",
      "lastName",
      "email",
      "hash",
      "phone",
    )
  }

}
