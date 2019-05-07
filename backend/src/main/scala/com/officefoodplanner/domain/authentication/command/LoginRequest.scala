package com.officefoodplanner.domain.authentication.command

final case class LoginRequest(
  username: String,
  password: String
)
