package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.model.{Permission, User, UserPermission}
import com.officefoodplanner.domain.auth.repository.UserPermissionRepository

class UserPermissionInMemoryRepository[F[_] : Applicative] extends UserPermissionRepository[F] {
  private val dao: InMemoryDao.Aux[F, UserPermission, UUID] =
    InMemoryDao[F, UserPermission](InMemoryDao.derive[F, UserPermission, UUID].apply(_.id))

  override def get(permissionId: UUID): F[Option[UserPermission]] = dao.get(permissionId)

  override def create(permission: Permission, user: User): F[UserPermission] =
    dao.create(UserPermission(userId = user.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): F[Int] = dao.deleteById(userPermissionId)

  override def listForUser(userId: UUID): F[List[UserPermission]] =
    Applicative[F].map(dao.listAll)(_.filter(_.userId == userId))

}


