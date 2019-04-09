package org.codecannery.lunchplanner.domain.authentication

import java.time.Instant
import java.util.UUID

import cats._
import cats.data._
import org.codecannery.lunchplanner.config.ApplicationConfig
import org.codecannery.lunchplanner.domain.authentication.command.LoginRequest
import org.codecannery.lunchplanner.domain.authentication.model.Session
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserAuthenticationFailedError, UserRepository}
import org.codecannery.lunchplanner.infrastructure.service.TransactingService
import tsec.common.{VerificationStatus, Verified}
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

abstract class AuthenticationService[F[_]: Monad, D[_]: Monad, H] extends TransactingService[F, D] {
  val applicationConfig: ApplicationConfig
  val sessionRepository: SessionRepository[D]
  val userRepository: UserRepository[D]
  val cryptService: PasswordHasher[D, H]

  def findAndProlongSession(sessionID: UUID): F[Option[(User, Session)]] = {
    val nextExpire = Instant.now().plusSeconds(applicationConfig.auth.sessionLength)

    transact(
      (for {
        session <- OptionT.apply[D, Session](sessionRepository.get(sessionID))
        user    <- OptionT.apply[D, User](userRepository.get(session.userID))
        _       <- OptionT.liftF(sessionRepository.update(session.copy(expires = nextExpire)))
      } yield (user, session)).value
    )
  }

  def authenticate(login: LoginRequest): F[Either[UserAuthenticationFailedError, Session]] = {
    def getUserOrFailLogin(login: LoginRequest) =
      EitherT.fromOptionF[D, UserAuthenticationFailedError, User](
        userRepository.findByUsername(login.username),
        UserAuthenticationFailedError(login.username)
      )

    def checkUserPassword(login: LoginRequest, user: User) =
      EitherT.liftF[D, UserAuthenticationFailedError, VerificationStatus](
        cryptService.checkpw(login.password, PasswordHash[H](user.hash)
      ))

    def loggedInUser(user: User) =
      EitherT.rightT[D, UserAuthenticationFailedError](user)

    def failedLoginForUsername(login: LoginRequest) =
      EitherT.leftT[D, User](UserAuthenticationFailedError(login.username))

    def createSession(user: User) = {
      EitherT.liftF[D, UserAuthenticationFailedError, Session](
      sessionRepository.create(Session(
        userID = user.key,
        expires = Instant.now().plusSeconds(applicationConfig.auth.sessionLength)
      )))
    }

    transact((for {
      user        <- getUserOrFailLogin(login)
      checkResult <- checkUserPassword(login, user)
      resp        <- if (checkResult == Verified) loggedInUser(user) else failedLoginForUsername(login)
      session     <- createSession(resp)
    } yield session).value)
  }

}
