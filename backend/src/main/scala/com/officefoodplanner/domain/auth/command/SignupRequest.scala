package com.officefoodplanner.domain.auth.command

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
