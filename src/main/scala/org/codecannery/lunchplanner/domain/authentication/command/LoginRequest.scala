package org.codecannery.lunchplanner.domain.authentication.command
import org.codecannery.lunchplanner.domain.users.command.CreateUser
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
  def asCreateUser[A](hashedPassword: PasswordHash[A]) : CreateUser = CreateUser(
    userName,
    firstName,
    lastName,
    email,
    hashedPassword.toString,
    phone
  )
}
