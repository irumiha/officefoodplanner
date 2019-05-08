package com.officefoodplanner.domain.auth.model

import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class Permission (
  id: UUID = UUID.randomUUID(),
  code: String,
  description: String
)

object Permission {
  implicit val keyedPermission: KeyEntity[Permission, UUID] = (e: Permission) => e.id
}
