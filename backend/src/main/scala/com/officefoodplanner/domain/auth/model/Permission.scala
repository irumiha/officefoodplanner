package com.officefoodplanner.domain.auth.model

import java.util.UUID

case class Permission (
  id: UUID = UUID.randomUUID(),
  code: String,
  description: String
)
