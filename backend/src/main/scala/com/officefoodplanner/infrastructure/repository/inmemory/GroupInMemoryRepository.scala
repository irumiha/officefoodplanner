package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID
import cats._
import com.officefoodplanner.domain.auth.model.Group
import com.officefoodplanner.domain.auth.repository.GroupRepository
import net.liftio.persistence.doobie.inmemory
import net.liftio.persistence.doobie.inmemory.InMemoryDao

class GroupInMemoryRepository[F[_] : Applicative] extends GroupRepository[F] {
  private val dao: InMemoryDao.Aux[F, Group, UUID] =
    inmemory.InMemoryDao.make[F, Group](InMemoryDao.derive[F, Group, UUID].apply(_.id))

  def create(user: Group): F[Group] = dao.create(user)

  def update(user: Group): F[Int] = dao.update(user)

  def get(userId: UUID): F[Option[Group]] = dao.get(userId)

  def list: F[List[Group]] = dao.listAll

  def findByGroupName(groupName: String): F[Option[Group]] =
    Applicative[F].map(dao.listAll)(_.find(_.name == groupName))
}



