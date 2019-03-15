package org.codecannery.lunchplanner.domain.users.model
import java.util.UUID

import io.circe._
import io.circe.generic.semiauto._
import org.codecannery.lunchplanner.infrastructure.repository._

case class User(userName: String,
                firstName: String,
                lastName: String,
                email: String,
                hash: String,
                phone: String,
                key: UUID = UUID.randomUUID())

object User {
  implicit object UuidKeyedUser extends UuidKeyEntity[User] {
    override def key(u: User): UUID = u.key
  }

  implicit val userDecoder: Decoder[User] = deriveDecoder
  implicit val userEncoder: Encoder[User] = deriveEncoder
}
