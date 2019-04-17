package org.codecannery.lunchplanner.domain.user.command

case class CreateUser(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    phone: String
)
