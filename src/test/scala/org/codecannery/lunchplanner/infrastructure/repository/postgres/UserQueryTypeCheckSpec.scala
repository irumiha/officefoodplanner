package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import org.scalatest._
import cats.effect.IO
import doobie.syntax.string._
import doobie.scalatest.IOChecker
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.postgres.implicits._
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.infrastructure.repository._
import org.codecannery.lunchplanner.infrastructure.LunchPlannerArbitraries.{user => userA}

class UserQueryTypeCheckSpec extends FunSuite with Matchers with IOChecker {
  import TableRepositorySQL._
  import UserTableRepository.doobieSupport

  override val transactor : Transactor[IO] = testTransactor

  val table = Table(SchemaName("public"), TableName("users"))

  test("Typecheck user queries") {
    userA.arbitrary.sample.foreach { u =>
      check(insertOne(table, u))
      check(select(table, fr"username = ${u.userName}", Fragment.empty, Fragment.empty, Fragment.empty))

      check(updateOne(table, u))
    }
    check(select[User](table, Fragment.empty, Fragment.empty, Fragment.empty, Fragment.empty))
    check(getMany[User, UUID](table, List(UUID.randomUUID())))
    check(deleteManyIDs[User, UUID](table, List(UUID.randomUUID())))
  }
}
