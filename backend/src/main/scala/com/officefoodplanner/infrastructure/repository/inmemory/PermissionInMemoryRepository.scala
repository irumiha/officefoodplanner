package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID
import cats._
import com.officefoodplanner.domain.auth.model.Permission
import com.officefoodplanner.domain.auth.repository.PermissionRepository
import net.liftio.persistence.doobie.inmemory
import net.liftio.persistence.doobie.inmemory.InMemoryDao

class PermissionInMemoryRepository[F[_] : Applicative] extends PermissionRepository[F] {
  private val dao: InMemoryDao.Aux[F, Permission, UUID] =
    inmemory.InMemoryDao.make[F, Permission](InMemoryDao.derive[F, Permission, UUID].apply(_.id))
  
  override def create(permission: Permission): F[Permission] = dao.create(permission)

  override def update(permission: Permission): F[Int] = dao.update(permission)

  override def findByPermissionCode(permissionCode: String): F[Option[Permission]] =
    Applicative[F].map(dao.listAll)(_.find(_.code == permissionCode))

  def get(permissionId: UUID): F[Option[Permission]] = dao.get(permissionId)

  def list: F[List[Permission]] = dao.listAll

}
