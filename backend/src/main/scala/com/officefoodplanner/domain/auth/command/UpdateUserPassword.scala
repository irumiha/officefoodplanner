package com.officefoodplanner.domain.auth.command

import java.util.UUID

case class UpdateUserPassword(
    oldPassword: String,
    newPassword: String,
    key: UUID
)
