package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.model.{Group, GroupPermission, Permission}
import com.officefoodplanner.domain.auth.repository.GroupPermissionRepository

class GroupPermissionInMemoryRepository[F[_] : Applicative] extends GroupPermissionRepository[F] {
  private val dao: InMemoryDao.Aux[F, GroupPermission, UUID] =
    InMemoryDao[F, GroupPermission](InMemoryDao.derive[F, GroupPermission, UUID].apply(_.id))

  override def get(permissionId: UUID): F[Option[GroupPermission]] = dao.get(permissionId)

  override def create(permission: Permission, group: Group): F[GroupPermission] =
    dao.create(GroupPermission(groupId = group.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): F[Int] = dao.deleteById(userPermissionId)

  override def listForGroup(groupId: UUID): F[List[GroupPermission]] =
    Applicative[F].map(dao.listAll)(_.filter(_.groupId == groupId))

}




