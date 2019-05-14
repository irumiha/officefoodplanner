package com.officefoodplanner.domain.auth.command

import java.util.UUID

case class UpdateUser(
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    key: UUID
)
