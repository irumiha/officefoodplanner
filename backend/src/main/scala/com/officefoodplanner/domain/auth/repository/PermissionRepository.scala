package com.officefoodplanner.domain.auth.repository

import java.util.UUID

import com.officefoodplanner.domain.auth.model.Permission

trait PermissionRepository[F[_]] {
  def create(permission: Permission): F[Permission]

  def get(permissionId: UUID): F[Option[Permission]]

  def update(permission: Permission): F[Int]

  def findByPermissionCode(permissionCode: String): F[Option[Permission]]

  def list: F[List[Permission]]
}
