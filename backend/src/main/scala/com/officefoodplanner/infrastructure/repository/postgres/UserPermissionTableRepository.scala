package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{Permission, User, UserPermission}
import com.officefoodplanner.domain.auth.repository.UserPermissionRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

object UserPermissionTableRepository extends UserPermissionRepository[ConnectionIO] {
  private val table = Table(SchemaName("auth"), TableName("user_permissions"))

  private val dao: TableDao.Aux[UserPermission, UUID] =
    TableDao[UserPermission](TableDao.derive[UserPermission, UUID](_.id, "id", table))

  override def get(permissionId: UUID): ConnectionIO[Option[UserPermission]] = dao.get(permissionId)

  override def create(permission: Permission, user: User): ConnectionIO[UserPermission] =
    dao.create(UserPermission(userId = user.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): ConnectionIO[Int] = dao.deleteById(userPermissionId)

  override def listForUser(userId: UUID): ConnectionIO[List[UserPermission]] =
    Query[UUID, UserPermission](
      s"select ${dao.columnsQuoted} from $table where user_id = ?"
      ).stream(userId).compile.toList
}
