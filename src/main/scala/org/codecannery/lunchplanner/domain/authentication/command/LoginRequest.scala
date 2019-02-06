package org.codecannery.lunchplanner.domain.authentication.command

final case class LoginRequest(
  userName: String,
  password: String
)

