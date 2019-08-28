package com.officefoodplanner.infrastructure.endpoint

import cats.effect._
import com.officefoodplanner.domain.auth.command.{CreateUser, LoginRequest, UpdateUserPassword}
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.infrastructure.OfficeFoodPlannerArbitraries
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl._
import org.http4s.headers.Location
import org.http4s.implicits._
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UserEndpointsSpec
    extends FunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with OfficeFoodPlannerArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  import PgApplicationSetup._


  test("create user") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: CreateUser =>
      (for {
        request <- POST(userSignup, uri"/")
        response <- userHttpEndpoints.run(request)
      } yield {
        response.status shouldEqual Ok
      }).unsafeRunSync
    }
  }

  test("update user") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: CreateUser =>
        (for {
          createRequest <- POST(userSignup, uri"/")
          createResponse <- userHttpEndpoints.run(createRequest)
          createdUser <- createResponse.as[User]
          userToUpdate = createdUser.copy(lastName = createdUser.lastName.reverse)
          updateUser <- PUT(userToUpdate, Uri.unsafeFromString(s"/${createdUser.username}"))
          updateResponse <- userHttpEndpoints.run(updateUser)
        } yield {
          updateResponse.status shouldEqual Forbidden
//          updatedUser.lastName shouldEqual createdUser.lastName.reverse
//          createdUser.key shouldEqual updatedUser.key
        }).unsafeRunSync
    }
  }

  test("update user password success") {
    val endpoints = newUserAndAuthHttpEndpoints

    forAll { userCreate: CreateUser =>
        (for {
          createRequest    <- POST(userCreate, Uri.uri("/users/"))
          createResponse   <- endpoints.run(createRequest)
          createdUser      <- createResponse.as[User]
          loginRequest     <- POST(LoginRequest(userCreate.username, userCreate.password), Uri.fromString("/auth/login?next=/").toOption.get)
          loginResponse    <- endpoints.run(loginRequest)
          sessionCookie = loginResponse.cookies.find(_.name == "session")
          updatePwRequest  <- PUT(UpdateUserPassword(userCreate.password, "NewPw1", createdUser.id), Uri.fromString(s"/users/change-password/${userCreate.username}").toOption.get)
          updateText       <- updatePwRequest.bodyAsText.compile.toList
          updatePwResponse <- endpoints.run(updatePwRequest.addCookie("session", sessionCookie.map(_.content).getOrElse("")))
          updatedUser      <- updatePwResponse.as[User]
          testRedirect     <- TemporaryRedirect(Location(uri"/"))
        } yield {
          loginResponse.status.code shouldEqual testRedirect.status.code
          updatePwResponse.status shouldEqual Ok
          updatedUser.passwordHash should not equal createdUser.passwordHash
        }).unsafeRunSync
    }
  }

  test("get user by userName") {
    val userHttpEndpoints = newUserHttpEndpoints

    forAll { userSignup: CreateUser =>
      (for {
        createRequest <- POST(userSignup, uri"/")
        createResponse <- userHttpEndpoints.run(createRequest)
        createdUser <- createResponse.as[User]
        getRequest <- GET(Uri.unsafeFromString(s"/${createdUser.username}"))
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

    forAll { userSignup: CreateUser =>
      (for {
        createRequest <- POST(userSignup, uri"/")
        createResponse <- userHttpEndpoints.run(createRequest)
        createdUser <- createResponse.as[User]
        deleteRequest <- DELETE(Uri.unsafeFromString(s"/${createdUser.username}"))
        deleteResponse <- userHttpEndpoints.run(deleteRequest)
        getRequest <- GET(Uri.unsafeFromString(s"/${createdUser.username}"))
        getResponse <- userHttpEndpoints.run(getRequest)
      } yield {
        createResponse.status shouldEqual Ok
        deleteResponse.status shouldEqual Forbidden
        getResponse.status shouldEqual Forbidden
      }).unsafeRunSync
    }
  }
}
