package org.codecannery.lunchplanner.domain.users.model
import java.util.UUID

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import org.codecannery.lunchplanner.infrastructure.repository._

case class User(userName: String,
                firstName: String,
                lastName: String,
                email: String,
                hash: String,
                phone: String,
                key: UUID = UUID.randomUUID()) {
  val table: Table = Table(SchemaName("public"), TableName("users"))
}

object User {
  def toJson(u: User): String = u.asJson.noSpaces
  def fromJson(s: String): Either[Error, User] = decode[User](s)

  implicit object UuidKeyedUser extends UuidKeyEntity[User] {
    override def key(u: User): UUID = u.key
  }
  implicit object UserTable extends TableEntity[User] {
    override def table(e: User): Table = e.table
  }
}
