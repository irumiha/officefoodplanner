package com.officefoodplanner.domain.auth.command

final case class LoginRequest(
  username: String,
  password: String
)
