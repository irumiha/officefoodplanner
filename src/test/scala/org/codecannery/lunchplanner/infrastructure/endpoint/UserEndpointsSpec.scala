package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.effect._
import org.codecannery.lunchplanner.domain.authentication.command.SignupRequest
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.LunchPlannerArbitraries
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl._
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UserEndpointsSpec
    extends FunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with LunchPlannerArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  import ApplicationSetup._


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
