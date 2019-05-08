package com.officefoodplanner.domain.auth.model

import java.time.Instant
import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class Session(
  id: UUID = UUID.randomUUID(),
  userID: UUID,
  createdOn: Instant = Instant.now(),
  expiresOn: Instant,
  updatedOn: Instant
)

object Session {
  implicit val keyed: KeyEntity[Session, UUID] = (u: Session) => u.id
}
