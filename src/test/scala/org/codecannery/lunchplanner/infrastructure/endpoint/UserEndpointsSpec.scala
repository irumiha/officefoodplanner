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

  private val cryptoService = BCrypt.syncPasswordHasher[IO]

  private val inMemoryUserRepo = new UserInMemoryRepository[IO]()

  private val userService = new UserService[IO, IO, BCrypt] {
    override val userRepo: UserRepository[IO] = inMemoryUserRepo
    override val cryptService: PasswordHasher[IO, BCrypt] = cryptoService
    override def transact[A](t: IO[A]): IO[A] = t
  }

  private val inMemorySessionRepo = new SessionInMemoryRepository[IO]()
  private val authService = new AuthenticationService[IO, IO, BCrypt] {
    override val applicationConfig: ApplicationConfig = TestConfig.appTestConfig
    override val sessionRepository: SessionRepository[IO] = inMemorySessionRepo
    override val userRepository: UserRepository[IO] = inMemoryUserRepo
    override val cryptService: PasswordHasher[IO, BCrypt] = cryptoService
    override def transact[A](t: IO[A]): IO[A] = t
  }

  private val authMiddleware = new Authenticate(TestConfig.appTestConfig, authService)
  private val userHttpService = UserEndpoints.endpoints(
    userService,
    authMiddleware
  ).orNotFound

  test("create user") {
    forAll { userSignup: SignupRequest =>
      (for {
        request <- POST(userSignup, Uri.uri("/"))
        response <- userHttpService.run(request)
      } yield {
        response.status shouldEqual Ok
      }).unsafeRunSync
    }
  }

  test("update user") {
    forAll { userSignup: SignupRequest =>
        (for {
          createRequest <- POST(userSignup, Uri.uri("/"))
          createResponse <- userHttpService.run(createRequest)
          createdUser <- createResponse.as[User]
          userToUpdate = createdUser.copy(lastName = createdUser.lastName.reverse)
          updateUser <- PUT(userToUpdate, Uri.unsafeFromString(s"/${createdUser.userName}"))
          updateResponse <- userHttpService.run(updateUser)
        } yield {
          updateResponse.status shouldEqual Forbidden
//          updatedUser.lastName shouldEqual createdUser.lastName.reverse
//          createdUser.key shouldEqual updatedUser.key
        }).unsafeRunSync
    }
  }

  test("get user by userName") {
    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/"))
        createResponse <- userHttpService.run(createRequest)
        createdUser <- createResponse.as[User]
        getRequest <- GET(Uri.unsafeFromString(s"/${createdUser.userName}"))
        getResponse <- userHttpService.run(getRequest)
//        getUser <- getResponse.as[User]
      } yield {
        getResponse.status shouldEqual Forbidden
//        createdUser.userName shouldEqual getUser.userName
      }).unsafeRunSync
    }
  }

  test("delete user by userName") {
    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/"))
        createResponse <- userHttpService.run(createRequest)
        createdUser <- createResponse.as[User]
        deleteRequest <- DELETE(Uri.unsafeFromString(s"/${createdUser.userName}"))
        deleteResponse <- userHttpService.run(deleteRequest)
        getRequest <- GET(Uri.unsafeFromString(s"/${createdUser.userName}"))
        getResponse <- userHttpService.run(getRequest)
      } yield {
        createResponse.status shouldEqual Ok
        deleteResponse.status shouldEqual Forbidden
        getResponse.status shouldEqual Forbidden
      }).unsafeRunSync
    }
  }
}
