package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.GroupPermissionRepository
import com.officefoodplanner.domain.auth.model.{Group, GroupPermission, Permission}

class GroupPermissionInMemoryRepository[F[_] : Applicative] extends GroupPermissionRepository[F] {

  private val repo = new InMemoryRepository[F, GroupPermission, UUID]

  override def get(permissionId: UUID): F[Option[GroupPermission]] = repo.get(permissionId)

  override def create(permission: Permission, group: Group): F[GroupPermission] =
    repo.create(GroupPermission(groupId = group.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): F[Int] = repo.deleteById(userPermissionId)

  override def listForGroup(groupId: UUID): F[List[GroupPermission]] =
    Applicative[F].map(repo.listAll)(_.filter(_.groupId == groupId))

}




