package com.officefoodplanner.domain.auth.model

import java.util.UUID

case class UserPermission (
  id: UUID = UUID.randomUUID(),
  userId: UUID,
  permissionId: UUID
)
