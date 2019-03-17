package org.codecannery.lunchplanner.domain.users.view

import java.util.UUID

case class UserListView(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    key: UUID
)
