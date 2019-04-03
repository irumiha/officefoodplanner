package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import org.scalatest._
import cats.effect.IO
import doobie.syntax.string._
import doobie.scalatest.IOChecker
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor

import org.codecannery.lunchplanner.infrastructure.repository._
import org.codecannery.lunchplanner.infrastructure.LunchPlannerArbitraries.{user => userA}

class UserQueryTypeCheckSpec extends FunSuite with Matchers with IOChecker {
  import JsonRepositorySQL._

  override val transactor : Transactor[IO] = testTransactor

  val table = Table(SchemaName("public"), TableName("users"))

  test("Typecheck user queries") {
    userA.arbitrary.sample.foreach { u =>
      check(insertOne(table, fromEntity(u)))
      check(select(table, fr"where data->>'userName' = ${u.userName}", Fragment.empty, Fragment.empty, Fragment.empty))

      check(updateOne(table, fromEntity(u)))
    }
    check(select(table, Fragment.empty, Fragment.empty, Fragment.empty, Fragment.empty))
    check(getMany(table, List(UUID.randomUUID())))
    check(deleteManyIDs(table, List(UUID.randomUUID())))
  }
}
