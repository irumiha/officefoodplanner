package com.officefoodplanner.domain.auth.model

import java.time.Instant
import java.util.UUID

case class Session(
  id: UUID = UUID.randomUUID(),
  userID: UUID,
  createdOn: Instant = Instant.now(),
  expiresOn: Instant,
  updatedOn: Instant
)
