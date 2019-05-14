package com.officefoodplanner.domain.auth.model

import java.time.Instant
import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class User(
    id: UUID = UUID.randomUUID(),
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    initialized: Boolean = false,
    createdOn: Instant,
    updatedOn: Instant
)

object User {
  implicit val keyedUser: KeyEntity[User, UUID] = e => e.id
}
