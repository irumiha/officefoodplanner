package com.officefoodplanner.infrastructure.endpoint

import cats.data.Kleisli
import cats.effect.IO
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth._
import com.officefoodplanner.domain.auth.command.CreateUser
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.repository.{SessionRepository, UserRepository}
import com.officefoodplanner.infrastructure.TestConfig
import com.officefoodplanner.infrastructure.middleware.Authenticate
import com.officefoodplanner.infrastructure.repository.inmemory.{SessionInMemoryRepository, UserInMemoryRepository}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder, Request, Response}
import tsec.passwordhashers.PasswordHasher
import tsec.passwordhashers.jca.BCrypt

object ApplicationSetup {
  implicit val userEnc: EntityEncoder[IO, User] = jsonEncoderOf
  implicit val userDec: EntityDecoder[IO, User] = jsonOf
  implicit val signupRequestEnc: EntityEncoder[IO, CreateUser] = jsonEncoderOf
  implicit val signupRequestDec: EntityDecoder[IO, CreateUser] = jsonOf

  def newUserService(
    customUserRepo: UserRepository[IO],
    customCryptService: PasswordHasher[IO, BCrypt]
  ): UserService[IO, IO, BCrypt] = {
    new UserService[IO, IO, BCrypt] {
      override val userRepo: UserRepository[IO] = customUserRepo
      override val cryptService: PasswordHasher[IO, BCrypt] = customCryptService

      override def transact[A](t: IO[A]): IO[A] = t
    }
  }

  def newAuthService(
    conf: ApplicationConfig,
    sessionR: SessionRepository[IO],
    userR: UserRepository[IO],
    cryptS: PasswordHasher[IO, BCrypt]
  ): AuthenticationService[IO, IO, BCrypt] = {
    new AuthenticationService[IO, IO, BCrypt] {
      override val applicationConfig: ApplicationConfig = conf
      override val sessionRepository: SessionRepository[IO] = sessionR
      override val userRepository: UserRepository[IO] = userR
      override val cryptService: PasswordHasher[IO, BCrypt] = cryptS

      override def transact[A](t: IO[A]): IO[A] = t
    }
  }

  def newUserHttpEndpoints: Kleisli[IO, Request[IO], Response[IO]] = {
    val cryptoService = BCrypt.syncPasswordHasher[IO]
    val inMemoryUserRepo = new UserInMemoryRepository[IO]()
    val inMemorySessionRepo = new SessionInMemoryRepository[IO]()

    val userService = newUserService(inMemoryUserRepo, cryptoService)
    val authService = newAuthService(TestConfig.appTestConfig, inMemorySessionRepo, inMemoryUserRepo, cryptoService)

    val authMiddleware = new Authenticate(TestConfig.appTestConfig, authService)

    UserEndpoints.endpoints(
      userService,
      authMiddleware
    ).orNotFound
  }
}
