package com.officefoodplanner.infrastructure.endpoint

import cats.data.Kleisli
import cats.effect.IO
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth._
import com.officefoodplanner.domain.auth.command.{CreateUser, LoginRequest, UpdateUserPassword}
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.{SessionRepository, UserRepository}
import com.officefoodplanner.infrastructure.TestConfig
import com.officefoodplanner.infrastructure.repository.postgres.testTransactor
import com.officefoodplanner.infrastructure.middleware.Authenticate
import com.officefoodplanner.infrastructure.repository.postgres.{SessionTableRepository, UserTableRepository}
import doobie._
import doobie.implicits._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, Request, Response}
import tsec.passwordhashers.PasswordHasher
import tsec.passwordhashers.jca.BCrypt

object PgApplicationSetup {
  implicit val userEnc: EntityEncoder[IO, User] = jsonEncoderOf
  implicit val userDec: EntityDecoder[IO, User] = jsonOf
  implicit val signupRequestEnc: EntityEncoder[IO, CreateUser] = jsonEncoderOf
  implicit val signupRequestDec: EntityDecoder[IO, CreateUser] = jsonOf
  implicit val loginRequestEnc: EntityEncoder[IO, LoginRequest] = jsonEncoderOf
  implicit val updatePwRequestEnc: EntityEncoder[IO, UpdateUserPassword] = jsonEncoderOf

  def newUserService(
    customUserRepo: UserRepository[ConnectionIO],
    customCryptService: PasswordHasher[ConnectionIO, BCrypt],
    conf: ApplicationConfig,
  ): UserService[IO, doobie.ConnectionIO, BCrypt] =
    new UserService[IO, ConnectionIO, BCrypt] {
      override val userRepo: UserRepository[ConnectionIO] = customUserRepo
      override val cryptService: PasswordHasher[ConnectionIO, BCrypt] = customCryptService
      override val applicationConfig: ApplicationConfig = conf

      override def transact[A](t: ConnectionIO[A]): IO[A] = t.transact(testTransactor)
    }

  def newAuthService(
    conf: ApplicationConfig,
    sessionR: SessionRepository[ConnectionIO],
    userR: UserRepository[ConnectionIO],
    cryptS: PasswordHasher[ConnectionIO, BCrypt]
  ): AuthenticationService[IO, doobie.ConnectionIO, BCrypt] =
    new AuthenticationService[IO, ConnectionIO, BCrypt] {
      override val applicationConfig: ApplicationConfig = conf
      override val sessionRepository: SessionRepository[ConnectionIO] = sessionR
      override val userRepository: UserRepository[ConnectionIO] = userR
      override val cryptService: PasswordHasher[ConnectionIO, BCrypt] = cryptS

      override def transact[A](t: ConnectionIO[A]): IO[A] = t.transact(testTransactor)
    }

  def newUserHttpEndpoints: Kleisli[IO, Request[IO], Response[IO]] = {
    val cryptoService = BCrypt.syncPasswordHasher[ConnectionIO]
    val inMemoryUserRepo = UserTableRepository
    val inMemorySessionRepo = SessionTableRepository

    val userService = newUserService(inMemoryUserRepo, cryptoService, TestConfig.appTestConfig)
    val authService = newAuthService(TestConfig.appTestConfig, inMemorySessionRepo, inMemoryUserRepo, cryptoService)

    val authMiddleware = new Authenticate(TestConfig.appTestConfig, authService)

    UserEndpoints.endpoints(
      userService,
      authMiddleware
    ).orNotFound
  }

  def newUserAndAuthHttpEndpoints: Kleisli[IO, Request[IO], Response[IO]] = {
    val cryptoService = BCrypt.syncPasswordHasher[ConnectionIO]
    val inMemoryUserRepo = UserTableRepository
    val inMemorySessionRepo = SessionTableRepository

    val userService = newUserService(inMemoryUserRepo, cryptoService, TestConfig.appTestConfig)
    val authService = newAuthService(TestConfig.appTestConfig, inMemorySessionRepo, inMemoryUserRepo, cryptoService)

    val authMiddleware = new Authenticate(TestConfig.appTestConfig, authService)

    Router(
      "/users" -> UserEndpoints.endpoints(userService,authMiddleware),
      "/auth"  -> AuthenticationEndpoints.endpoints(TestConfig.appTestConfig,authService),
    ).orNotFound
  }
}
