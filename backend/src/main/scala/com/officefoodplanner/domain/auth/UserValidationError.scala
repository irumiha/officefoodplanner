package com.officefoodplanner.domain.auth

import com.officefoodplanner.domain.DomainError

sealed trait UserValidationError extends DomainError
case object UserNotFoundError extends UserValidationError
case class UserAlreadyExistsError(userName: String) extends UserValidationError
case class UserAuthenticationFailedError(userName: String) extends UserValidationError
case object OldPasswordMismatch extends UserValidationError
case object ChangeNotAllowed extends UserValidationError
case class NewPasswordError(message: String) extends UserValidationError
