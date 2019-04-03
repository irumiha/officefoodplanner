package org.codecannery.lunchplanner.domain.user.model
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

  implicit val uuidKeyedUser: UuidKeyEntity[User] = (u: User) => u.key

  implicit val jsonDecoder: Decoder[User] = deriveDecoder
  implicit val jsonEncoder: Encoder[User] = deriveEncoder
}
