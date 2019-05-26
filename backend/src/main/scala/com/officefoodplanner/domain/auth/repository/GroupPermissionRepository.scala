package com.officefoodplanner.domain.auth.repository

import java.util.UUID

import com.officefoodplanner.domain.auth.model.{Group, GroupPermission, Permission}

trait GroupPermissionRepository[F[_]] {
  def create(permission: Permission, group: Group): F[GroupPermission]

  def get(groupPermissionId: UUID): F[Option[GroupPermission]]

  def delete(groupPermissionId: UUID): F[Int]

  def listForGroup(groupId: UUID): F[List[GroupPermission]]
}
