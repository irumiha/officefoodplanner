package com.officefoodplanner.domain.authentication.command

import com.officefoodplanner.domain.user.command.CreateUser

final case class SignupRequest(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    phone: String,
) {
  def asCreateUser: CreateUser = CreateUser(
    userName,
    firstName,
    lastName,
    email,
    password,
    phone
  )
}
