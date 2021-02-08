package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID
import com.officefoodplanner.domain.auth.model.Group
import com.officefoodplanner.domain.auth.repository.GroupRepository
import com.officefoodplanner.infrastructure.repository.{SchemaName, Table, TableName}
import doobie._
import doobie.postgres.implicits._
import net.liftio.persistence
import net.liftio.persistence.doobie.postgres.TableDao

object GroupTableRepository extends GroupRepository[ConnectionIO] {
  private val table = Table(SchemaName("auth"), TableName("groups"))
  private val dao: TableDao.Aux[Group, UUID] =
    persistence.doobie.postgres.TableDao.make[Group](TableDao.derive[Group, UUID](_.id, "id", table))

  def create(user: Group): ConnectionIO[Group] = dao.create(user)

  def update(user: Group): ConnectionIO[Int] = dao.update(user)

  def get(userId: UUID): ConnectionIO[Option[Group]] = dao.get(userId)

  def list: ConnectionIO[List[Group]] = dao.listAll

  def findByGroupName(groupName: String): ConnectionIO[Option[Group]] =
    Query[String, Group](
      s"select ${dao.columnsQuoted} from $table where name = ?"
    ).option(groupName)
}
