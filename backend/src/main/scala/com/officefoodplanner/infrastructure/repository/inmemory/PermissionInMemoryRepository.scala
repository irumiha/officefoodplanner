package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.PermissionRepository
import com.officefoodplanner.domain.auth.model.Permission

class PermissionInMemoryRepository[F[_] : Applicative] extends PermissionRepository[F] {

  private val repo = new InMemoryRepository[F, Permission, UUID]

  override def create(permission: Permission): F[Permission] = repo.create(permission)

  override def update(permission: Permission): F[Int] = repo.update(permission)

  override def findByPermissionCode(permissionCode: String): F[Option[Permission]] =
    Applicative[F].map(repo.listAll)(_.find(_.code == permissionCode))

  def get(permissionId: UUID): F[Option[Permission]] = repo.get(permissionId)

  def list: F[List[Permission]] = repo.listAll

}


