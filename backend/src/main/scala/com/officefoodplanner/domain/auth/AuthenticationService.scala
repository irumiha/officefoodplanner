package com.officefoodplanner.domain.auth

import java.time.Instant
import java.util.UUID

import cats._
import cats.data._
import cats.implicits._
import cats.effect.Timer
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.{SessionRepository, UserRepository}
import com.officefoodplanner.infrastructure.service.TransactingService
import tsec.common.{VerificationStatus, Verified}
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

abstract class AuthenticationService[F[_]: Monad, D[_]: Monad, H] extends TransactingService[F, D] {
  val applicationConfig: ApplicationConfig
  val sessionRepository: SessionRepository[D]
  val userRepository: UserRepository[D]
  val cryptService: PasswordHasher[D, H]

  def findAndProlongSession(sessionID: UUID): F[Option[(User, model.Session)]] = {
    val nextExpire = Instant.now().plusSeconds(applicationConfig.auth.sessionLength)

    transact(
      (for {
        session <- OptionT(sessionRepository.get(sessionID))
        user    <- OptionT(userRepository.get(session.userID))
        _       <- OptionT.liftF(sessionRepository.update(session.copy(expiresOn = nextExpire, updatedOn = Instant.now())))
      } yield (user, session)).value
    )
  }

  def authenticate(login: command.LoginRequest): F[Either[UserAuthenticationFailedError, model.Session]] = {
    def getUserOrFailLogin(login: command.LoginRequest): EitherT[D, UserAuthenticationFailedError, User] =
      EitherT.fromOptionF[D, UserAuthenticationFailedError, User](
        userRepository.findByUsername(login.username),
        UserAuthenticationFailedError(login.username)
      )

    def checkUserPassword(login: command.LoginRequest, user: User) =
      EitherT.liftF[D, UserAuthenticationFailedError, VerificationStatus](
        cryptService.checkpw(login.password, PasswordHash[H](user.passwordHash)))

    def loggedInUser(user: User) =
      EitherT.rightT[D, UserAuthenticationFailedError](user)

    def failedLoginForUsername(login: command.LoginRequest) =
      EitherT.leftT[D, User](UserAuthenticationFailedError(login.username))

    def createSession(user: User) =
      EitherT.liftF[D, UserAuthenticationFailedError, model.Session](
        sessionRepository.create(
          model.Session(
            userID = user.id,
            expiresOn = Instant.now().plusSeconds(applicationConfig.auth.sessionLength),
            createdOn = Instant.now(),
            updatedOn = Instant.now()
          )))

    transact((for {
      user        <- getUserOrFailLogin(login)
      checkResult <- checkUserPassword(login, user)
      resp        <- if (checkResult == Verified) loggedInUser(user) else failedLoginForUsername(login)
      session     <- createSession(resp)
    } yield session).value)
  }

}
