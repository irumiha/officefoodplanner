package com.officefoodplanner.domain.auth.model

import java.util.UUID

case class UserGroup (
  id: UUID = UUID.randomUUID(),
  groupId: UUID,
  userId: UUID
)
