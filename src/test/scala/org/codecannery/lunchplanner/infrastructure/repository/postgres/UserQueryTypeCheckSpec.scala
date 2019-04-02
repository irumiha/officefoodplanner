package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import org.scalatest._
import cats.effect.IO
import doobie.syntax.string._
import doobie.scalatest.IOChecker
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor

import org.codecannery.lunchplanner.infrastructure.LunchPlannerArbitraries.user
import org.codecannery.lunchplanner.infrastructure.repository

class UserQueryTypeCheckSpec extends FunSuite with Matchers with IOChecker {
  override val transactor : Transactor[IO] = testTransactor
  val table = repository.Table(repository.SchemaName("public"), repository.TableName("users"))
  import JsonRepositorySQL._

  test("Typecheck user queries") {
    user.arbitrary.sample.foreach { u =>
      check(insertOne(table, fromEntity(u)))
      check(select(table, fr"where data->>'userName' = ${u.userName}", Fragment.empty, Fragment.empty, Fragment.empty))

      check(updateOne(table, fromEntity(u)))
    }
    check(select(table, Fragment.empty, Fragment.empty, Fragment.empty, Fragment.empty))
    check(getMany(table, List(UUID.randomUUID())))
    check(deleteManyIDs(table, List(UUID.randomUUID())))
  }
}
