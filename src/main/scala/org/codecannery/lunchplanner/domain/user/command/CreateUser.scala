package org.codecannery.lunchplanner.domain.user.command

case class CreateUser(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String
)
