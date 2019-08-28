package com.officefoodplanner.domain.auth.model

import java.time.Instant
import java.util.UUID

case class User(
    id: UUID = UUID.randomUUID(),
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    passwordHash: String,
    initialized: Boolean = false,
    createdOn: Instant,
    updatedOn: Instant,
    active: Boolean = false,
)
