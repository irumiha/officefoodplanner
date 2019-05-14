package com.officefoodplanner.domain.auth.model

import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class UserGroup (
  id: UUID = UUID.randomUUID(),
  groupId: UUID,
  userId: UUID
)

object UserGroup {
  implicit val keyed: KeyEntity[UserGroup, UUID] = e => e.id
}
