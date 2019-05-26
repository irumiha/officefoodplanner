package com.officefoodplanner.domain.auth.repository

import java.util.UUID

import com.officefoodplanner.domain.auth.model.Group

trait GroupRepository[F[_]] {
  def create(group: Group): F[Group]

  def get(groupId: UUID): F[Option[Group]]

  def update(group: Group): F[Int]

  def findByGroupName(groupName: String): F[Option[Group]]

  def list: F[List[Group]]
}
