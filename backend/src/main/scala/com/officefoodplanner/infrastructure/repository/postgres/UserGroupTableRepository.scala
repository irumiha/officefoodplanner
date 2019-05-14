package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.UserGroupRepository
import com.officefoodplanner.domain.auth.model.{Group, User, UserGroup}
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

class UserGroupTableRepository extends UserGroupRepository[ConnectionIO] {

  import UserGroupTableRepository.doobieSupport

  private val repo: TableRepository[UserGroup, UUID] = new TableRepository[UserGroup, UUID] {
    override val table = Table(SchemaName("auth"), TableName("user_groups"))
  }

  override def get(userGroupId: UUID): ConnectionIO[Option[UserGroup]] = repo.get(userGroupId)

  override def create(user: User, group: Group): ConnectionIO[UserGroup] =
    repo.create(UserGroup(userId = user.id, groupId = group.id))

  override def delete(userGroupId: UUID): ConnectionIO[Int] = repo.deleteById(userGroupId)

  override def listForUser(userId: UUID): ConnectionIO[List[UserGroup]] =
    repo.find(fr"user_id = $userId", None, None, None)

}

object UserGroupTableRepository {
  implicit val doobieSupport: DoobieSupport[UserGroup] = new DoobieSupport[UserGroup] {
    override def columns: List[DoobieColumn[UserGroup]] = List(
      id,
      DoobieColumn[UserGroup]("user_id", p => fr0"${p.userId}"),
      DoobieColumn[UserGroup]("group_id", p => fr0"${p.groupId}"),
    )

    override def id: DoobieColumn[UserGroup] = DoobieColumn[UserGroup]("id", ug => fr0"${ug.id}")
  }
}
