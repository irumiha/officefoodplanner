package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}
import tsec.common.Verified
import tsec.passwordhashers.{PasswordHash, PasswordHasher}
import org.codecannery.lunchplanner.domain.authentication.command.{LoginRequest, SignupRequest}
import org.codecannery.lunchplanner.domain.user.UserService
import org.codecannery.lunchplanner.domain.user.command.{CreateUser, UpdateUser}
import org.codecannery.lunchplanner.domain.user.model.User
import org.codecannery.lunchplanner.domain.{
  UserAlreadyExistsError,
  UserAuthenticationFailedError,
  UserNotFoundError
}
import org.codecannery.lunchplanner.infrastructure.endpoint.Pagination.{OffsetQ, PageSizeQ}

import scala.language.higherKinds

class UserEndpoints[F[_]: Effect, A](
    userService: UserService[F],
    cryptService: PasswordHasher[F, A])
    extends Http4sDsl[F] {
  /* Jsonization of our User type */

  implicit val userUpdateDecoder: EntityDecoder[F, UpdateUser] = jsonOf
  implicit val userCreateDecoder: EntityDecoder[F, CreateUser] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf
  implicit val signupReqDecoder: EntityDecoder[F, SignupRequest] = jsonOf

  def endpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "login"       => login(req)
      case req @ POST -> Root / "users"       => signup(req)
      case req @ PUT -> Root / "users" / name => update(req, name)
      case GET -> Root / "users" :? PageSizeQ(pageSize) :? OffsetQ(offset) =>
        list(pageSize, offset)
      case GET -> Root / "users" / username    => searchByUsername(username)
      case DELETE -> Root / "users" / username => deleteByUsername(username)
    }

  private def login(req: Request[F]): F[Response[F]] = {
    val action: EitherT[F, UserAuthenticationFailedError, User] = for {
      login <- EitherT.liftF(req.as[LoginRequest])
      user <- userService
        .getUserByUsername(login.userName)
        .leftMap(_ => UserAuthenticationFailedError(login.userName))
      checkResult <- EitherT.liftF(cryptService.checkpw(login.password, PasswordHash[A](user.hash)))
      resp <- if (checkResult == Verified)
        EitherT.rightT[F, UserAuthenticationFailedError](user)
      else
        EitherT.leftT[F, User](UserAuthenticationFailedError(login.userName))
    } yield resp

    action.value.flatMap {
      case Right(user) => Ok(user.asJson)
      case Left(UserAuthenticationFailedError(name)) =>
        BadRequest(s"Authentication failed for user $name")
    }
  }

  private def signup(req: Request[F]): F[Response[F]] = {
    val action = for {
      signup <- req.as[SignupRequest]
      hash <- cryptService.hashpw(signup.password)
      user <- signup.asCreateUser(hash).pure[F]
      result <- userService.createUser(user).value
    } yield result

    action.flatMap {
      case Right(saved) => Ok(saved.asJson)
      case Left(UserAlreadyExistsError(existing)) =>
        Conflict(s"The user with user name $existing already exists")
    }
  }

  private def update(req: Request[F], name: String): F[Response[F]] = {
    val action = for {
      user <- req.as[UpdateUser]
      updated = user.copy(userName = name)
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
    }

  private def deleteByUsername(username: String): F[Response[F]] =
    for {
      _ <- userService.deleteByUsername(username)
      resp <- Ok()
    } yield resp

}

object UserEndpoints {
  def endpoints[F[_]: Effect, A](
      userService: UserService[F],
      cryptService: PasswordHasher[F, A]
  ): HttpRoutes[F] =
    new UserEndpoints[F, A](userService, cryptService).endpoints
}
