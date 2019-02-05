package org.codecannery.lunchplanner.domain.users.command

case class UpdateUser(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    id: Option[Long] = None
)
