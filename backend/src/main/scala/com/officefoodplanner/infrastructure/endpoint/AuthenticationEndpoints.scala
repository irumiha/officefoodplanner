package com.officefoodplanner.infrastructure.endpoint

import java.time.Instant

import cats.effect._
import cats.implicits._
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.{AuthenticationService, UserAuthenticationFailedError}
import com.officefoodplanner.domain.auth.command.LoginRequest
import com.officefoodplanner.domain.auth.model.Session
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location

class AuthenticationEndpoints[F[_], D[_], H](
    config: ApplicationConfig,
    authService: AuthenticationService[F, D, H]
)(implicit F: Effect[F]) extends Http4sDsl[F] {
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf

  def endpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "login" :? Qnext(next) => login(req, next)
    }

  private def newSessionCookie(sessionData: Session) = {
    val cookieExpiresAt = Instant.now().plusSeconds(config.auth.cookieDuration)

    ResponseCookie(
      name = config.auth.sessionCookieName,
      content = sessionData.id.toString,
      expires = HttpDate.fromInstant(cookieExpiresAt).toOption,
    )
  }

  private def login(req: Request[F], nextJump: String): F[Response[F]] = {
    val loginResult = for {
      nextUri  <- F.pure(Uri.fromString(nextJump)).rethrow
      request  <- req.as[LoginRequest]
      auth     <- authService.authenticate(request)
      resp     <- TemporaryRedirect(Location(nextUri))
    } yield resp.addCookie(newSessionCookie(auth))

    loginResult.handleErrorWith {
      case UserAuthenticationFailedError(name) =>
        BadRequest(s"Authentication failed for user $name")
      case ParseFailure(_,_) =>
        BadRequest(s"Invalid next URI")
    }
  }

  object Qnext extends QueryParamDecoderMatcher[String]("next")
}

object AuthenticationEndpoints {
  def endpoints[F[_]: Effect, D[_], H](
    config: ApplicationConfig,
    authService: AuthenticationService[F, D, H]
  ): HttpRoutes[F] =
    new AuthenticationEndpoints[F, D, H](config, authService).endpoints

}
