package org.codecannery.lunchplanner.domain.authentication.model

import java.time.Instant
import java.util.UUID

import org.codecannery.lunchplanner.infrastructure.repository.KeyEntity

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