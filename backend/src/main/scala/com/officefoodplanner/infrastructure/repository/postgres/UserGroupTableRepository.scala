package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{Group, User, UserGroup}
import com.officefoodplanner.domain.auth.repository.UserGroupRepository
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
    override val id: DoobieColumn[UserGroup] = DoobieColumn("id")(_.id)
    override val columns: List[DoobieColumn[UserGroup]] = List(
      id,
      DoobieColumn("user_id" )(_.userId),
      DoobieColumn("group_id")(_.groupId),
    )
  }
}