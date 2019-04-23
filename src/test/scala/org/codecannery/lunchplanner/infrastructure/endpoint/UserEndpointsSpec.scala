package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.effect._
import io.circe.generic.auto._
import org.codecannery.lunchplanner.config.ApplicationConfig
import org.codecannery.lunchplanner.domain.authentication.command.SignupRequest
import org.codecannery.lunchplanner.domain.authentication.{AuthenticationService, SessionRepository}
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserRepository, UserService}
import org.codecannery.lunchplanner.infrastructure.middleware.Authenticate
import org.codecannery.lunchplanner.infrastructure.repository.inmemory.{SessionInMemoryRepository, UserInMemoryRepository}
import org.codecannery.lunchplanner.infrastructure.{LunchPlannerArbitraries, TestConfig}
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl._
import org.http4s.implicits._
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import tsec.passwordhashers.PasswordHasher
import tsec.passwordhashers.jca.BCrypt

class UserEndpointsSpec
    extends FunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with LunchPlannerArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  implicit val userEnc: EntityEncoder[IO, User] = jsonEncoderOf
  implicit val userDec: EntityDecoder[IO, User] = jsonOf
  implicit val signupRequestEnc: EntityEncoder[IO, SignupRequest] = jsonEncoderOf
  implicit val signupRequestDec: EntityDecoder[IO, SignupRequest] = jsonOf

  private def newUserService(
    customUserRepo: UserRepository[IO],
    customCryptService: PasswordHasher[IO, BCrypt]
  ) = {
    new UserService[IO, IO, BCrypt] {
      override val userRepo: UserRepository[IO] = customUserRepo
      override val cryptService: PasswordHasher[IO, BCrypt] = customCryptService

      override def transact[A](t: IO[A]): IO[A] = t
    }
  }

  private def newAuthService(
    conf: ApplicationConfig,
    sessionR: SessionRepository[IO],
    userR: UserRepository[IO],
    cryptS: PasswordHasher[IO, BCrypt]
  ) = {
    new AuthenticationService[IO, IO, BCrypt] {
      override val applicationConfig: ApplicationConfig = conf
      override val sessionRepository: SessionRepository[IO] = sessionR
      override val userRepository: UserRepository[IO] = userR
      override val cryptService: PasswordHasher[IO, BCrypt] = cryptS

      override def transact[A](t: IO[A]): IO[A] = t
    }
  }

  private def newUserHttpEndpoints = {
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

  test("create user") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: SignupRequest =>
      (for {
        request <- POST(userSignup, Uri.uri("/"))
        response <- userHttpEndpoints.run(request)
      } yield {
        response.status shouldEqual Ok
      }).unsafeRunSync
    }
  }

  test("update user") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: SignupRequest =>
        (for {
          createRequest <- POST(userSignup, Uri.uri("/"))
          createResponse <- userHttpEndpoints.run(createRequest)
          createdUser <- createResponse.as[User]
          userToUpdate = createdUser.copy(lastName = createdUser.lastName.reverse)
          updateUser <- PUT(userToUpdate, Uri.unsafeFromString(s"/${createdUser.userName}"))
          updateResponse <- userHttpEndpoints.run(updateUser)
        } yield {
          updateResponse.status shouldEqual Forbidden
//          updatedUser.lastName shouldEqual createdUser.lastName.reverse
//          createdUser.key shouldEqual updatedUser.key
        }).unsafeRunSync
    }
  }

  test("get user by userName") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/"))
        createResponse <- userHttpEndpoints.run(createRequest)
        createdUser <- createResponse.as[User]
        getRequest <- GET(Uri.unsafeFromString(s"/${createdUser.userName}"))
        getResponse <- userHttpEndpoints.run(getRequest)
//        getUser <- getResponse.as[User]
      } yield {
        getResponse.status shouldEqual Forbidden
//        createdUser.userName shouldEqual getUser.userName
      }).unsafeRunSync
    }
  }

  test("delete user by userName") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/"))
        createResponse <- userHttpEndpoints.run(createRequest)
        createdUser <- createResponse.as[User]
        deleteRequest <- DELETE(Uri.unsafeFromString(s"/${createdUser.userName}"))
        deleteResponse <- userHttpEndpoints.run(deleteRequest)
        getRequest <- GET(Uri.unsafeFromString(s"/${createdUser.userName}"))
        getResponse <- userHttpEndpoints.run(getRequest)
      } yield {
        createResponse.status shouldEqual Ok
        deleteResponse.status shouldEqual Forbidden
        getResponse.status shouldEqual Forbidden
      }).unsafeRunSync
    }
  }
}
