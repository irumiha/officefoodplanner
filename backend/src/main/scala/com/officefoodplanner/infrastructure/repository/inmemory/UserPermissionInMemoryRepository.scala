package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.UserPermissionRepository
import com.officefoodplanner.domain.auth.model.{Permission, User, UserPermission}

class UserPermissionInMemoryRepository[F[_] : Applicative] extends UserPermissionRepository[F] {

  private val repo = new InMemoryRepository[F, UserPermission, UUID]

  override def get(permissionId: UUID): F[Option[UserPermission]] = repo.get(permissionId)

  override def create(permission: Permission, user: User): F[UserPermission] =
    repo.create(UserPermission(userId = user.id, permissionId = permission.id))

  override def delete(userPermissionId: UUID): F[Int] = repo.deleteById(userPermissionId)

  override def listForUser(userId: UUID): F[List[UserPermission]] =
    Applicative[F].map(repo.listAll)(_.filter(_.userId == userId))

}


