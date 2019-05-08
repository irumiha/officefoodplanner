package com.officefoodplanner.infrastructure.middleware

import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import java.util.UUID

import com.officefoodplanner.config.ApplicationConfig
import com.officefoodplanner.domain.auth.AuthenticationService
import com.officefoodplanner.domain.auth.model.User

class Authenticate[F[_]: Effect, D[_], H](
  config: ApplicationConfig,
  authService: AuthenticationService[F, D, H]
) {
  def retrieveUser: Kleisli[F, RequestCookie, Either[String, User]] = Kleisli( cookie =>
    authService.findAndProlongSession(UUID.fromString(cookie.content)).map{ u =>
      Either.fromOption(u, "Session does not exist").map(_._1)
    }
  )
  val authUser: Kleisli[F, Request[F], Either[String,User]] = Kleisli({ request =>
    val message = for {
      header <- headers.Cookie.from(request.headers).toRight("Cookie parsing error")
      cookie <- header.values.toList.find(_.name == config.auth.sessionCookieName).toRight("Couldn't find the authcookie")
    } yield cookie

    (for {
      m <- EitherT.fromEither[F](message)
      result <- EitherT(retrieveUser.run(m))
    } yield result).value
  })

}
