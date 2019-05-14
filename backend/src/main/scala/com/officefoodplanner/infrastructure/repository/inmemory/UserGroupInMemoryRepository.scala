package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.UserGroupRepository
import com.officefoodplanner.domain.auth.model.{Group, User, UserGroup}

class UserGroupInMemoryRepository[F[_] : Applicative] extends UserGroupRepository[F] {

  private val repo = new InMemoryRepository[F, UserGroup, UUID]

  override def get(userGroupId: UUID): F[Option[UserGroup]] = repo.get(userGroupId)

  override def create(user: User, group: Group): F[UserGroup] =
    repo.create(UserGroup(userId = user.id, groupId = group.id))

  override def delete(userGroupId: UUID): F[Int] = repo.deleteById(userGroupId)

  override def listForUser(userId: UUID): F[List[UserGroup]] =
    Applicative[F].map(repo.listAll)(_.filter(_.userId == userId))

}


