package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.infrastructure.repository.postgres.TableDao
import com.officefoodplanner.infrastructure.repository.{SchemaName, Table, TableName}
import com.officefoodplanner.domain.auth.model.{Group, GroupPermission, Permission}
import com.officefoodplanner.domain.auth.repository.GroupPermissionRepository
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

object GroupPermissionTableRepository extends GroupPermissionRepository[ConnectionIO] {
  private val table = Table(SchemaName("auth"), TableName("group_permissions"))
  private val dao: TableDao.Aux[GroupPermission, UUID] =
    TableDao[GroupPermission](TableDao.derive[GroupPermission, UUID](_.id, "id", table))

  override def get(permissionId: UUID): ConnectionIO[Option[GroupPermission]] = dao.get(permissionId)

  override def create(permission: Permission, group: Group): ConnectionIO[GroupPermission] =
    dao.create(GroupPermission(groupId = group.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): ConnectionIO[Int] = dao.deleteById(userPermissionId)

  override def listForGroup(groupId: UUID): ConnectionIO[List[GroupPermission]] =
    Query[UUID, GroupPermission](
      s"select ${dao.columnsQuoted} from $table where group_id = ?"
    ).stream(groupId).compile.toList

}
