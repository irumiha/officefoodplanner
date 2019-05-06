package org.codecannery.lunchplanner

import io.circe.Decoder
import io.circe.generic.semiauto._

package object config {
  implicit val srDec: Decoder[ServerConfig] = deriveDecoder
  implicit val dbDec: Decoder[DatabaseConfig] = deriveDecoder
  implicit val dbconnDec: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val authDec: Decoder[AuthConfig] = deriveDecoder
  implicit val csrfDec: Decoder[CsrfConfig] = deriveDecoder
  implicit val oauthOffice365Dec: Decoder[OauthDetails] = deriveDecoder
  implicit val oauthDec: Decoder[Oauth] = deriveDecoder

  implicit val psDec: Decoder[ApplicationConfig] = deriveDecoder
}
