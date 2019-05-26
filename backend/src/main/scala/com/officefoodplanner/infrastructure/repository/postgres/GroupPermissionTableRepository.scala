package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{Group, GroupPermission, Permission}
import com.officefoodplanner.domain.auth.repository.GroupPermissionRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

class GroupPermissionTableRepository extends GroupPermissionRepository[ConnectionIO] {

  import GroupPermissionTableRepository.doobieSupport

  private val repo: TableRepository[GroupPermission, UUID] = new TableRepository[GroupPermission, UUID] {
    override val table = Table(SchemaName("auth"), TableName("group_permissions"))
  }

  override def get(permissionId: UUID): ConnectionIO[Option[GroupPermission]] = repo.get(permissionId)

  override def create(permission: Permission, group: Group): ConnectionIO[GroupPermission] =
    repo.create(GroupPermission(groupId = group.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): ConnectionIO[Int] = repo.deleteById(userPermissionId)

  override def listForGroup(groupId: UUID): ConnectionIO[List[GroupPermission]] =
    repo.find(fr"group_id = $groupId", None, None, None)

}

object GroupPermissionTableRepository {
  implicit val doobieSupport: DoobieSupport[GroupPermission] = new DoobieSupport[GroupPermission] {
    override def columns: List[DoobieColumn[GroupPermission]] = List(
      id,
      DoobieColumn[GroupPermission]("group_id", c => fr0"${c.groupId}"),
      DoobieColumn[GroupPermission]("permission_id", c => fr0"${c.permissionId}")
    )

    override def id: DoobieColumn[GroupPermission] = DoobieColumn[GroupPermission]("id", c => fr0"${c.id}")
  }
}


