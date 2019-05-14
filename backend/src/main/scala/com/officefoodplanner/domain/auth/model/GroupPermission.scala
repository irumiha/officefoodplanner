package com.officefoodplanner.domain.auth.model

import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class GroupPermission (
  id: UUID = UUID.randomUUID(),
  groupId: UUID,
  permissionId: UUID
)

object GroupPermission {
  implicit val keyed: KeyEntity[GroupPermission, UUID] = e => e.id
}
