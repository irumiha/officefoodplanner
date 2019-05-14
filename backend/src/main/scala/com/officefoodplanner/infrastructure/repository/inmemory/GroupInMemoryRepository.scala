package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import com.officefoodplanner.domain.auth.GroupRepository
import com.officefoodplanner.domain.auth.model.Group

class GroupInMemoryRepository[F[_] : Applicative] extends GroupRepository[F] {

  private val repo = new InMemoryRepository[F, Group, UUID]

  def create(user: Group): F[Group] = repo.create(user)

  def update(user: Group): F[Int] = repo.update(user)

  def get(userId: UUID): F[Option[Group]] = repo.get(userId)

  def list: F[List[Group]] = repo.listAll

  def findByGroupName(groupName: String): F[Option[Group]] =
    Applicative[F].map(repo.listAll)(_.find(_.name == groupName))
}



