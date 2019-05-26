package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.Group
import com.officefoodplanner.domain.auth.repository.GroupRepository
import com.officefoodplanner.infrastructure.repository.{DoobieColumn, DoobieSupport, SchemaName, Table, TableName}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

class GroupTableRepository extends GroupRepository[ConnectionIO] {

  import GroupTableRepository.doobieSupport

  private val repo: TableRepository[Group, UUID] = new TableRepository[Group, UUID] {
    override val table = Table(SchemaName("auth"), TableName("groups"))
  }

  private def byName(groupName: String) = fr"name = $groupName"

  def create(user: Group): ConnectionIO[Group] = repo.create(user)

  def update(user: Group): ConnectionIO[Int] = repo.update(user)

  def get(userId: UUID): ConnectionIO[Option[Group]] = repo.get(userId)

  def list: ConnectionIO[List[Group]] = repo.listAll

  def findByGroupName(groupName: String): ConnectionIO[Option[Group]] =
    repo.find(byName(groupName), None, None, None).map(_.headOption)
}

object GroupTableRepository {
  implicit val doobieSupport: DoobieSupport[Group] = new DoobieSupport[Group] {
    override def id: DoobieColumn[Group] = DoobieColumn[Group]("id", g => fr0"${g.id}")

    override def columns: List[DoobieColumn[Group]] = List(
      DoobieColumn[Group]("id",   g => fr0"${g.id}"),
      DoobieColumn[Group]("name", g => fr0"${g.name}"),
    )
  }
}

