package com.officefoodplanner.infrastructure

import java.time.Instant

import com.officefoodplanner.domain.authentication.command.SignupRequest
import com.officefoodplanner.domain.user.model.User
import org.scalacheck._

trait OfficeFoodPlannerArbitraries {

  val userNameLength = 16
  val userNameGen: Gen[String] = Gen.listOfN(userNameLength, Gen.alphaChar).map(_.mkString)

  implicit val instant: Arbitrary[Instant] = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit val user: Arbitrary[User] = Arbitrary[User] {
    for {
      userName <- userNameGen
      firstName <- userNameGen
      lastName <- userNameGen
      email <- userNameGen
      password <- userNameGen
      phone <- userNameGen
      id <- Gen.uuid
      now <- instant.arbitrary
    } yield User(id, userName, firstName, lastName, email, password, phone, now, now)
  }

  implicit val userSignup: Arbitrary[SignupRequest] = Arbitrary[SignupRequest] {
    for {
      userName <- userNameGen
      firstName <- userNameGen
      lastName <- userNameGen
      email <- userNameGen
      password <- userNameGen
      phone <- userNameGen
    } yield SignupRequest(userName, firstName, lastName, email, password, phone)
  }
}

object OfficeFoodPlannerArbitraries extends OfficeFoodPlannerArbitraries
