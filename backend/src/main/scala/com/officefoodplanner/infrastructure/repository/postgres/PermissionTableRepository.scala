package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID
import com.officefoodplanner.domain.auth.model.Permission
import com.officefoodplanner.domain.auth.repository.PermissionRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.postgres.implicits._
import net.liftio.persistence
import net.liftio.persistence.doobie.postgres.TableDao

object PermissionTableRepository extends PermissionRepository[ConnectionIO] {
  private val table = Table(SchemaName("auth"), TableName("permissions"))
  private val dao: TableDao.Aux[Permission, UUID] =
    persistence.doobie.postgres.TableDao.make[Permission](TableDao.derive[Permission, UUID](_.id, "id", table))

  override def create(permission: Permission): ConnectionIO[Permission] = dao.create(permission)

  override def update(permission: Permission): ConnectionIO[Int] = dao.update(permission)

  override def findByPermissionCode(permissionCode: String): ConnectionIO[Option[Permission]] =
    Query[String, Permission](
      s"select ${dao.columnsQuoted} from $table where group_id = ?"
    ).option(permissionCode)

  override def get(permissionId: UUID): ConnectionIO[Option[Permission]] = dao.get(permissionId)

  override def list: ConnectionIO[List[Permission]] = dao.listAll

}
