package com.officefoodplanner.domain.auth.model

import java.util.UUID

case class GroupPermission (
  id: UUID = UUID.randomUUID(),
  groupId: UUID,
  permissionId: UUID
)

