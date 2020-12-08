package com.officefoodplanner.infrastructure.endpoint

import cats.data.{Kleisli, OptionT}
import cats.effect._
import cats._
import cats.implicits._
import com.officefoodplanner.domain.auth.command.{CreateUser, LoginRequest, UpdateUser, UpdateUserPassword}
import com.officefoodplanner.domain.auth.model.User
import com.officefoodplanner.domain.auth.view.UserSimpleView
import com.officefoodplanner.domain.auth.{ChangeNotAllowed, NewPasswordError, OldPasswordMismatch, UserAlreadyExistsError, UserNotFoundError, UserService, UserValidationError}
import com.officefoodplanner.infrastructure.endpoint.Pagination.{OffsetQ, PageSizeQ}
import com.officefoodplanner.infrastructure.middleware.Authenticate
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware

class UserEndpoints[F[_]: Effect, D[_], H](
    userService: UserService[F, D, H],
    authMiddleware: Authenticate[F, D, H]
) extends Http4sDsl[F] {

  implicit val userUpdateDecoder: EntityDecoder[F, UpdateUser] = jsonOf
  implicit val userCreateDecoder: EntityDecoder[F, CreateUser] = jsonOf
  implicit val updatePwDecoder: EntityDecoder[F, UpdateUserPassword] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf

  def nonAuthEndpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root => signup(req)
    }

  def authEndpoints: HttpRoutes[F] =
    authM(AuthedRoutes.of[User, F] {
      case       GET    -> Root :? PageSizeQ(ps) :? OffsetQ(o) as _            => list(ps, o)
      case       GET    -> Root / username                     as _            => searchByUsername(username)
      case req @ PUT    -> Root / name                         as _            => update(req, name)
      case req @ PUT    -> Root / "change-password" / name     as _            => updatePassword(req, name)
      case       GET    -> Root / "loggedin"                   as loggedInUser => showLoggedInUser(loggedInUser)
    })

  val onAuthFailure: AuthedRoutes[String, F] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))
  val authM: AuthMiddleware[F, User] =
    AuthMiddleware(authMiddleware.authUser, onAuthFailure)

  private def signup(req: Request[F]): F[Response[F]] = {
    val action = for {
      signup <- req.as[CreateUser]
      result <- userService.createUser(signup).value
    } yield result

    action.flatMap {
      case Right(saved)                           => Ok(saved.asJson)
      case Left(UserAlreadyExistsError(existing)) => Conflict(s"The user with user name $existing already exists")
      case _                                      => InternalServerError(s"Oooof this error hurts!")
    }
  }

  private def update(req: AuthedRequest[F, User], name: String): F[Response[F]] = {
    val action = for {
      user <- req.req.as[UpdateUser]
      updated = user.copy(username = name)
      result <- userService.update(updated).value
    } yield result

    action.flatMap {
      case Right(saved)            => Ok(saved.asJson)
      case Left(UserNotFoundError) => NotFound("User not found.")
      case Left(UserAlreadyExistsError(username)) =>
        NotFound(s"Username $username not available.")
      case _ => InternalServerError("Unexpected error")
    }
  }

  private def updatePassword(req: AuthedRequest[F, User], name: String): F[Response[F]] = {
    val action =
      if (req.context.username == name) {
        for {
          updatePasswordRequest <- req.req.as[UpdateUserPassword]
          result                <- userService.updatePassword(name, updatePasswordRequest).value
        } yield result
      } else {
        Monad[F].pure(Either.left[UserValidationError, User](ChangeNotAllowed))
      }

    action.flatMap {
      case Right(saved)                => Ok(saved.asJson)
      case Left(UserNotFoundError)     => NotFound("User not found.")
      case Left(ChangeNotAllowed)      => BadRequest(s"You are not allowed to change password for user $name")
      case Left(OldPasswordMismatch)   => BadRequest(s"Old password does not match")
      case Left(NewPasswordError(msg)) => BadRequest(msg)
      case _ => InternalServerError("Unexpected error")
    }
  }

  private def list(
      pageSize: Option[Int],
      offset: Option[Int]
  ): F[Response[F]] =
    for {
      retrieved <- userService.list(pageSize.getOrElse(10), offset.getOrElse(0))
      resp <- Ok(retrieved.asJson)
    } yield resp

  private def searchByUsername(username: String): F[Response[F]] =
    userService.getUserByUsername(username).value.flatMap {
      case Right(found)            => Ok(found.asJson)
      case Left(UserNotFoundError) => NotFound("The user was not found")
      case _                       => InternalServerError(s"Oooof this error hurts even more!")
    }

  def showLoggedInUser(loggedInUser: User): F[Response[F]] = {
    Ok(loggedInUser.into[UserSimpleView].transform.asJson)
  }

}

object UserEndpoints {
  def endpoints[F[_]: Effect, D[_], H](
      userService: UserService[F, D, H],
      authMiddleware: Authenticate[F, D, H]
  ): HttpRoutes[F] = {
    val userEndpoints = new UserEndpoints[F, D, H](userService, authMiddleware)

    userEndpoints.nonAuthEndpoints <+>
    userEndpoints.authEndpoints
  }
}
