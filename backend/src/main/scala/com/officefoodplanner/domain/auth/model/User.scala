package com.officefoodplanner.domain.auth.model

import java.time.Instant
import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class User(
    id: UUID = UUID.randomUUID(),
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    passwordHash: String,
    initialized: Boolean = false,
    active: Boolean = false,
    createdOn: Instant,
    updatedOn: Instant
)

object User {
  implicit val keyedUser: KeyEntity[User, UUID] = e => e.id
}
