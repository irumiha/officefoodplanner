package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import org.scalatest._
import cats.effect.IO
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.infrastructure.repository.{SchemaName, Table, TableName}
import doobie.syntax.string._
import doobie.scalatest.IOChecker
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.postgres.implicits._

class UserQueryTypeCheckSpec extends FunSuite with Matchers with IOChecker {
  import TableRepositorySQL._
  import UserTableRepository.doobieSupport
  import com.officefoodplanner.infrastructure.OfficeFoodPlannerArbitraries.user

  override val transactor : Transactor[IO] = testTransactor

  val table = Table(SchemaName("auth"), TableName("users"))

  test("Typecheck user queries") {
    user.arbitrary.sample.foreach { u =>
      check(insertOne(table, u))
      check(insertMany(table))
      check(select(table, fr"username = ${u.username}", Fragment.empty, Fragment.empty, Fragment.empty))
      check(updateOne[User, UUID](table, u))
    }
    check(select[User](table, Fragment.empty, Fragment.empty, Fragment.empty, Fragment.empty))
    check(getMany[User, UUID](table, List(UUID.randomUUID())))
    check(deleteManyIDs[User, UUID](table, List(UUID.randomUUID())))
    check(deleteBy(table, fr"id = ${UUID.randomUUID()}"))
  }
}
