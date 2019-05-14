package com.officefoodplanner.domain.auth.model

import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class UserPermission (
  id: UUID = UUID.randomUUID(),
  userId: UUID,
  permissionId: UUID
)

object UserPermission {
  implicit val keyed: KeyEntity[UserPermission, UUID] = e => e.id
}
