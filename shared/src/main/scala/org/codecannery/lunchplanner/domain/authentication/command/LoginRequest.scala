package org.codecannery.lunchplanner.domain.authentication.command

final case class LoginRequest(
  username: String,
  password: String
)
