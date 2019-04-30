package org.codecannery.lunchplanner.domain.authentication.model

import java.time.Instant
import java.util.UUID

import io.circe._
import io.circe.generic.semiauto._
import org.codecannery.lunchplanner.infrastructure.repository.KeyEntity

case class Session(
  key: UUID = UUID.randomUUID(),
  userID: UUID,
  created: Instant = Instant.now(),
  expires: Instant
)

object Session {
  implicit val keyed: KeyEntity[Session, UUID] = (u: Session) => u.key

  implicit val jsonDecoder: Decoder[Session] = deriveDecoder
  implicit val jsonEncoder: Encoder[Session] = deriveEncoder

}
