package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{Permission, User, UserPermission}
import com.officefoodplanner.domain.auth.repository.UserPermissionRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

class UserPermissionTableRepository extends UserPermissionRepository[ConnectionIO] {

  import UserPermissionTableRepository.doobieSupport

  private val repo: TableRepository[UserPermission, UUID] = new TableRepository[UserPermission, UUID] {
    override val table = Table(SchemaName("auth"), TableName("user_permissions"))
  }

  override def get(permissionId: UUID): ConnectionIO[Option[UserPermission]] = repo.get(permissionId)

  override def create(permission: Permission, user: User): ConnectionIO[UserPermission] =
    repo.create(UserPermission(userId = user.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): ConnectionIO[Int] = repo.deleteById(userPermissionId)

  override def listForUser(userId: UUID): ConnectionIO[List[UserPermission]] =
    repo.find(fr"user_id = $userId", None, None, None)

}

object UserPermissionTableRepository {
  implicit val doobieSupport: DoobieSupport[UserPermission] = new DoobieSupport[UserPermission] {
    override val id: DoobieColumn[UserPermission] = DoobieColumn[UserPermission]("id", p => fr0"${p.id}")
    override val columns: List[DoobieColumn[UserPermission]] = List(
      id,
      DoobieColumn[UserPermission]("user_id", p => fr0"${p.userId}"),
      DoobieColumn[UserPermission]("permission_id", p => fr0"${p.permissionId}")
    )
  }
}
