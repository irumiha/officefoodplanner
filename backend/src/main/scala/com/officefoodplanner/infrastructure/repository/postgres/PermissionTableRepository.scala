package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.Permission
import com.officefoodplanner.domain.auth.repository.PermissionRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

class PermissionTableRepository extends PermissionRepository[ConnectionIO] {

  import PermissionTableRepository.doobieSupport

  private val repo: TableRepository[Permission, UUID] = new TableRepository[Permission, UUID] {
    override val table = Table(SchemaName("auth"), TableName("permissions"))
  }

  private def byCode(code: String) = fr"code = $code"

  override def create(permission: Permission): ConnectionIO[Permission] = repo.create(permission)

  override def update(permission: Permission): ConnectionIO[Int] = repo.update(permission)

  override def findByPermissionCode(permissionCode: String): ConnectionIO[Option[Permission]] =
    repo.find(byCode(permissionCode), None, None, None).map(_.headOption)

  def get(permissionId: UUID): ConnectionIO[Option[Permission]] = repo.get(permissionId)

  def list: ConnectionIO[List[Permission]] = repo.listAll

}

object PermissionTableRepository {
  implicit val doobieSupport: DoobieSupport[Permission] = new DoobieSupport[Permission] {
    override def columns: List[DoobieColumn[Permission]] = List(
      id,
      DoobieColumn[Permission]("code",        p => fr0"${p.code}"),
      DoobieColumn[Permission]("description", p => fr0"${p.description}"),
    )

    override def id: DoobieColumn[Permission] = DoobieColumn[Permission]("id", p => fr0"${p.id}")
  }
}
