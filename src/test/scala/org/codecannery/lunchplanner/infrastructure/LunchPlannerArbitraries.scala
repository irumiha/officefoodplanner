package org.codecannery.lunchplanner.infrastructure

import java.time.Instant

import org.codecannery.lunchplanner.domain.authentication.command.SignupRequest
import org.codecannery.lunchplanner.domain.user.model.User
import org.scalacheck._

trait LunchPlannerArbitraries {

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
    } yield User(userName, firstName, lastName, email, password, phone, id)
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

object LunchPlannerArbitraries extends LunchPlannerArbitraries
