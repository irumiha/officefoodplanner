package com.officefoodplanner.domain.auth.command

import java.util.UUID

case class UpdateUserContactData(
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    key: UUID
)
