package com.officefoodplanner.domain.auth

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{User, UserPermission, Permission}

trait UserPermissionRepository[F[_]] {
  def create(permission: Permission, user: User): F[UserPermission]

  def get(userPermissionId: UUID): F[Option[UserPermission]]

  def delete(userPermissionId: UUID): F[Int]

  def listForUser(userId: UUID): F[List[UserPermission]]
}
