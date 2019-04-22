package org.codecannery.lunchplanner.domain.user

import java.util.UUID

import org.codecannery.lunchplanner.domain.user.model.User

trait UserRepository[F[_]] {
  def create(user: User): F[User]

  def update(user: User): F[Int]

  def get(userId: UUID): F[Option[User]]

  def deleteById(userId: UUID): F[Int]

  def findByUsername(userName: String): F[Option[User]]

  def deleteByUserName(userName: String): F[Int]

  def list: F[List[User]]
}
