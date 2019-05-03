package org.codecannery.lunchplanner.domain.user.model

import java.time.Instant
import java.util.UUID

import org.codecannery.lunchplanner.infrastructure.repository._

case class User(
    id: UUID = UUID.randomUUID(),
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    createdOn: Instant,
    updatedOn: Instant
)

object User {
  implicit val keyedUser: KeyEntity[User, UUID] = (e: User) => e.id
}
