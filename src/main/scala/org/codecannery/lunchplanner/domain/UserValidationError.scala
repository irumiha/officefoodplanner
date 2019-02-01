package org.codecannery.lunchplanner.domain

import org.codecannery.lunchplanner.domain.users.User

sealed trait UserValidationError extends Product with Serializable
case object UserNotFoundError extends UserValidationError
case class UserAlreadyExistsError(user: User) extends UserValidationError
case class UserAuthenticationFailedError(userName: String) extends UserValidationError
case class UsernameAlredyTakenError(username: String) extends UserValidationError
