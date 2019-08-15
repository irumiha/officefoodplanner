package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.model.{Group, User, UserGroup}
import com.officefoodplanner.domain.auth.repository.UserGroupRepository

class UserGroupInMemoryRepository[F[_] : Applicative] extends UserGroupRepository[F] {
  private val dao: InMemoryDao.Aux[F, UserGroup, UUID] =
    InMemoryDao[F, UserGroup](InMemoryDao.derive[F, UserGroup, UUID].apply(_.id))

  override def get(userGroupId: UUID): F[Option[UserGroup]] = dao.get(userGroupId)

  override def create(user: User, group: Group): F[UserGroup] =
    dao.create(UserGroup(userId = user.id, groupId = group.id))

  override def delete(userGroupId: UUID): F[Int] = dao.deleteById(userGroupId)

  override def listForUser(userId: UUID): F[List[UserGroup]] =
    Applicative[F].map(dao.listAll)(_.filter(_.userId == userId))

}


