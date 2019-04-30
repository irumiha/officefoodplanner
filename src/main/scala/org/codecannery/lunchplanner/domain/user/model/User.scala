package org.codecannery.lunchplanner.domain.user.model

import java.util.UUID

import io.circe._
import io.circe.generic.semiauto._
import org.codecannery.lunchplanner.infrastructure.repository._

case class User(
    key: UUID = UUID.randomUUID(),
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
)

object User {

  implicit val keyedUser: KeyEntity[User, UUID] = (e: User) => e.key

  implicit val jsonDecoder: Decoder[User] = deriveDecoder
  implicit val jsonEncoder: Encoder[User] = deriveEncoder
}
