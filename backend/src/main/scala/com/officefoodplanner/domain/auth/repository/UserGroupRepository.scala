package com.officefoodplanner.domain.auth.repository

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{Group, User, UserGroup}

trait UserGroupRepository[F[_]] {
  def create(user: User, group: Group): F[UserGroup]

  def get(userGroupId: UUID): F[Option[UserGroup]]

  def delete(userGroupId: UUID): F[Int]

  def listForUser(userId: UUID): F[List[UserGroup]]
}
