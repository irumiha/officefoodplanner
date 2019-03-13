package org.codecannery.lunchplanner.domain.users.command

case class CreateUser(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    id: Option[Long] = None
)