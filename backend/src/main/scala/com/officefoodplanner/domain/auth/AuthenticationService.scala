package com.officefoodplanner.domain.auth

import java.time.Instant
import java.util.UUID

import cats._
import cats.data._
import cats.syntax.all._
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.model.{Session, User}
import com.officefoodplanner.domain.auth.repository.{SessionRepository, UserRepository}
import com.officefoodplanner.infrastructure.service.TransactingService
import tsec.common.Verified
import tsec.passwordhashers.{PasswordHash, PasswordHasher}

abstract class AuthenticationService[F[_], D[_], H]()
  (implicit MEF: MonadError[F, Throwable], MED: MonadError[D, Throwable])
  extends TransactingService[F, D] {
  val applicationConfig: ApplicationConfig
  val sessionRepository: SessionRepository[D]
  val userRepository: UserRepository[D]
  val cryptService: PasswordHasher[D, H]

  def findAndProlongSession(sessionID: UUID): F[Option[(User, model.Session)]] = {
    val nextExpire = Instant.now().plusSeconds(applicationConfig.auth.sessionLength)

    def updateSession(session: Session) =
      sessionRepository.update(
        session.copy(expiresOn = nextExpire, updatedOn = Instant.now())
      )

    transact(
      (for {
        session <- OptionT(sessionRepository.get(sessionID))
        user    <- OptionT(userRepository.get(session.userID))
        _       <- OptionT.liftF(updateSession(session))
      } yield (user, session)).value
    )
  }

  def authenticate(login: command.LoginRequest): F[model.Session] = {
    val failed = UserAuthenticationFailedError(login.username)

    def getUserOrFailLogin(login: command.LoginRequest) =
      userRepository
        .findByUsername(login.username)
        .ensure(failed)(_.isDefined)
        .map(u => u.get)

    def checkUserPassword(login: command.LoginRequest, user: User) =
      cryptService
        .checkpw(login.password, PasswordHash[H](user.passwordHash))
        .ensure(failed)(_ == Verified)

    def createSession(user: User) =
      sessionRepository.create(
        model.Session(
          userID = user.id,
          expiresOn = Instant.now().plusSeconds(applicationConfig.auth.sessionLength),
          createdOn = Instant.now(),
          updatedOn = Instant.now()
        ))

    val session = for {
      user        <- getUserOrFailLogin(login)
      _           <- checkUserPassword(login, user)
      session     <- createSession(user)
    } yield session

    transact(session)
  }

}
