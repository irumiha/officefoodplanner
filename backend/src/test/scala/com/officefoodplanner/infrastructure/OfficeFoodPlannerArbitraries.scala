package com.officefoodplanner.infrastructure

import java.time.Instant

import com.officefoodplanner.domain.auth.command.CreateUser
import com.officefoodplanner.domain.auth.model.User
import org.scalacheck._

trait OfficeFoodPlannerArbitraries {

  val userNameLength = 16
  val randomStringGen: Gen[String] = Gen.listOfN(userNameLength, Gen.alphaChar).map(_.mkString)

  implicit val instant: Arbitrary[Instant] = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit val user: Arbitrary[User] = Arbitrary[User] {
    for {
      userName <- randomStringGen
      firstName <- randomStringGen
      lastName <- randomStringGen
      email <- randomStringGen
      password <- randomStringGen
      id <- Gen.uuid
      now <- instant.arbitrary
    } yield User(id, userName, firstName, lastName, email, password, initialized = false, active=true, now, now)
  }

  implicit val userSignup: Arbitrary[CreateUser] = Arbitrary[CreateUser] {
    for {
      userName <- randomStringGen
      firstName <- randomStringGen
      lastName <- randomStringGen
      email <- randomStringGen
      password <- randomStringGen
    } yield CreateUser(userName, firstName, lastName, email, password)
  }
}

object OfficeFoodPlannerArbitraries extends OfficeFoodPlannerArbitraries
