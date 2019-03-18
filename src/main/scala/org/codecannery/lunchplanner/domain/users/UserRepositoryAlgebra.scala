package org.codecannery.lunchplanner.domain.users

import java.util.UUID

import org.codecannery.lunchplanner.domain.users.model.User

trait UserRepositoryAlgebra[F[_]] {
  def create(user: User): F[User]

  def update(user: User): F[Option[User]]

  def get(userId: UUID): F[Option[User]]

  def delete(userId: UUID): F[Option[User]]

  def findByUserName(userName: String): F[Option[User]]

  def deleteByUserName(userName: String): F[Option[User]]

  def list(pageSize: Int, offset: Int): F[List[User]]
}
