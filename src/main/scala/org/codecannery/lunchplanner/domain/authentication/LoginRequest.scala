package org.codecannery.lunchplanner.domain.authentication

import org.codecannery.lunchplanner.domain.users.User
import tsec.passwordhashers.PasswordHash

final case class LoginRequest(
  userName: String,
  password: String
)

final case class SignupRequest(
  userName: String,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  phone: String,
){
  def asUser[A](hashedPassword: PasswordHash[A]) : User = User(
    userName,
    firstName,
    lastName,
    email,
    hashedPassword.toString,
    phone
  )
}
