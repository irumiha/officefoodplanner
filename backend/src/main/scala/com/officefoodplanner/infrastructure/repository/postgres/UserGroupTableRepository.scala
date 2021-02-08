package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID
import com.officefoodplanner.domain.auth.model.{Group, User, UserGroup}
import com.officefoodplanner.domain.auth.repository.UserGroupRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import net.liftio.persistence
import net.liftio.persistence.doobie.postgres.TableDao

object UserGroupTableRepository extends UserGroupRepository[ConnectionIO] {

  private val table = Table(SchemaName("auth"), TableName("user_groups"))

  private val dao: TableDao.Aux[UserGroup, UUID] =
    persistence.doobie.postgres.TableDao.make[UserGroup](TableDao.derive[UserGroup, UUID](_.id, "id", table))

  override def get(userGroupId: UUID): ConnectionIO[Option[UserGroup]] = dao.get(userGroupId)

  override def create(user: User, group: Group): ConnectionIO[UserGroup] =
    dao.create(UserGroup(userId = user.id, groupId = group.id))

  override def delete(userGroupId: UUID): ConnectionIO[Int] = dao.deleteById(userGroupId)

  override def listForUser(userId: UUID): ConnectionIO[List[UserGroup]] =
    Query[UUID, UserGroup](
      s"select ${dao.columnsQuoted} from $table where user_id = ?"
    ).stream(userId).compile.toList

}
