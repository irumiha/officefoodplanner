package org.codecannery.lunchplanner.domain.user

sealed trait UserValidationError extends Product with Serializable
case object UserNotFoundError extends UserValidationError
case class UserAlreadyExistsError(userName: String) extends UserValidationError
case class UserAuthenticationFailedError(userName: String) extends UserValidationError