package com.officefoodplanner.infrastructure.endpoint

import java.time.Instant

import cats.data._
import cats.effect._
import cats.implicits._
import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.{AuthenticationService, UserAuthenticationFailedError}
import com.officefoodplanner.domain.auth.command.LoginRequest
import com.officefoodplanner.domain.auth.model.Session
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location

class AuthenticationEndpoints[F[_]: Effect, D[_], H](
    config: ApplicationConfig,
    authService: AuthenticationService[F, D, H]
) extends Http4sDsl[F] {
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf

  def endpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "login" => login(req)
    }

  private def newSessionCookie(sessionData: Session) = {
    val cookieExpiresAt = Instant.now().plusSeconds(config.auth.cookieDuration)

    ResponseCookie(
      name = config.auth.sessionCookieName,
      content = sessionData.id.toString,
      expires = HttpDate.fromInstant(cookieExpiresAt).toOption,
    )
  }

  private def login(req: Request[F]): F[Response[F]] = {
    Uri.fromString(req.params("next")).map { nextJump =>
      val loginResult = (for {
        request  <- EitherT.liftF(req.as[LoginRequest])
        auth     <- EitherT.apply(authService.authenticate(request))
      } yield auth).value

      loginResult.flatMap {
        case Right(session) =>
              TemporaryRedirect(Location(nextJump))
                .map(_.addCookie(newSessionCookie(session)))
        case Left(UserAuthenticationFailedError(name)) =>
          BadRequest(s"Authentication failed for user $name")
      }
    }.getOrElse(BadRequest(s"Invalid next parameter"))
  }
}

object AuthenticationEndpoints {
  def endpoints[F[_]: Effect, D[_], H](
    config: ApplicationConfig,
    authService: AuthenticationService[F, D, H]
  ): HttpRoutes[F] =
    new AuthenticationEndpoints[F, D, H](config, authService).endpoints

}