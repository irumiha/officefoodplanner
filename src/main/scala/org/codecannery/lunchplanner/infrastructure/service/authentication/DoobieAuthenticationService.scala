package org.codecannery.lunchplanner.infrastructure.service.authentication

import cats._
import doobie._
import doobie.implicits._
import org.codecannery.lunchplanner.config.ApplicationConfig
import org.codecannery.lunchplanner.domain.authentication.{AuthenticationService, SessionRepository}
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.UserRepository
import tsec.passwordhashers.PasswordHasher

class DoobieAuthenticationService[F[_] : Monad](
  val applicationConfig: ApplicationConfig,
  val sessionRepository: SessionRepository[ConnectionIO],
  val userRepository: UserRepository[ConnectionIO],
  xa: Transactor[F],
  val cryptService: PasswordHasher[ConnectionIO, User]
) extends AuthenticationService[F, ConnectionIO] {
  override def transact[A](t: ConnectionIO[A]): F[A] = t.transact(xa)
}
