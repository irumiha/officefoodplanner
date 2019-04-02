package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.effect._
import io.circe.generic.auto._
import org.codecannery.lunchplanner.domain.authentication.command.SignupRequest
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.user.{UserService, UserValidationInterpreter}
import org.codecannery.lunchplanner.infrastructure.LunchPlannerArbitraries
import org.codecannery.lunchplanner.infrastructure.repository.postgres.UserJsonRepository
import org.codecannery.lunchplanner.infrastructure.repository.postgres.testTransactor

import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl._
import org.http4s.implicits._
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import tsec.passwordhashers.jca.BCrypt

class UserEndpointsSpec
  extends FunSuite
  with Matchers
  with ScalaCheckPropertyChecks
  with LunchPlannerArbitraries
  with Http4sDsl[IO]
  with Http4sClientDsl[IO] {

  implicit val userEnc          : EntityEncoder[IO, User]          = jsonEncoderOf
  implicit val userDec          : EntityDecoder[IO, User]          = jsonOf
  implicit val signupRequestEnc : EntityEncoder[IO, SignupRequest] = jsonEncoderOf
  implicit val signupRequestDec : EntityDecoder[IO, SignupRequest] = jsonOf

  test("create user") {
    val userRepo = new UserJsonRepository()
    val userValidation = UserValidationInterpreter(userRepo)
    val userService = UserService[IO](userRepo, userValidation, testTransactor)
    val userHttpService = UserEndpoints.endpoints(userService, BCrypt.syncPasswordHasher[IO]).orNotFound

    forAll { userSignup: SignupRequest =>
      (for {
        request <- POST(userSignup, Uri.uri("/users"))
        response <- userHttpService.run(request)
      } yield {
        response.status shouldEqual Ok
      }).unsafeRunSync
    }
  }

  test("update user") {
    val userRepo = new UserJsonRepository()
    val userValidation = UserValidationInterpreter(userRepo)
    val userService = UserService[IO](userRepo, userValidation, testTransactor)
    val userHttpService = UserEndpoints.endpoints(userService, BCrypt.syncPasswordHasher[IO]).orNotFound

    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/users"))
        createResponse <- userHttpService.run(createRequest)
        createdUser <- createResponse.as[User]
        userToUpdate = createdUser.copy(lastName = createdUser.lastName.reverse)
        updateUser <- PUT(userToUpdate, Uri.unsafeFromString(s"/users/${createdUser.userName}"))
        updateResponse <- userHttpService.run(updateUser)
        updatedUser <- updateResponse.as[User]
      } yield {
        updateResponse.status shouldEqual Ok
        updatedUser.lastName shouldEqual createdUser.lastName.reverse
        createdUser.key shouldEqual updatedUser.key
      }).unsafeRunSync
    }
  }

  test("get user by userName") {
    val userRepo = new UserJsonRepository()
    val userValidation = UserValidationInterpreter(userRepo)
    val userService = UserService[IO](userRepo, userValidation, testTransactor)
    val userHttpService = UserEndpoints.endpoints(userService, BCrypt.syncPasswordHasher[IO]).orNotFound

    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/users"))
        createResponse <- userHttpService.run(createRequest)
        createdUser <- createResponse.as[User]
        getRequest <- GET(Uri.unsafeFromString(s"/users/${createdUser.userName}"))
        getResponse <- userHttpService.run(getRequest)
        getUser <- getResponse.as[User]
      } yield {
        getResponse.status shouldEqual Ok
        createdUser.userName shouldEqual getUser.userName
      }).unsafeRunSync
    }
  }


  test("delete user by userName") {
    val userRepo = new UserJsonRepository()
    val userValidation = UserValidationInterpreter(userRepo)
    val userService = UserService[IO](userRepo, userValidation, testTransactor)
    val userHttpService = UserEndpoints.endpoints(userService, BCrypt.syncPasswordHasher[IO]).orNotFound

    forAll { userSignup: SignupRequest =>
      (for {
        createRequest <- POST(userSignup, Uri.uri("/users"))
        createResponse <- userHttpService.run(createRequest)
        createdUser <- createResponse.as[User]
        deleteRequest <- DELETE(Uri.unsafeFromString(s"/users/${createdUser.userName}"))
        deleteResponse <- userHttpService.run(deleteRequest)
        getRequest <- GET(Uri.unsafeFromString(s"/users/${createdUser.userName}"))
        getResponse <- userHttpService.run(getRequest)
      } yield {
        createResponse.status shouldEqual Ok
        deleteResponse.status shouldEqual Ok
        getResponse.status shouldEqual NotFound
      }).unsafeRunSync
    }
  }
}
