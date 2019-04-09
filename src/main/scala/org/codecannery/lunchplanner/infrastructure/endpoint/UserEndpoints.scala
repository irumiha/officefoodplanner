package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.codecannery.lunchplanner.domain.authentication.command.{LoginRequest, SignupRequest}
import org.codecannery.lunchplanner.domain.user.{UserService, _}
import org.codecannery.lunchplanner.infrastructure.endpoint.Pagination.{OffsetQ, PageSizeQ}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import tsec.passwordhashers.PasswordHasher

import scala.language.higherKinds

class UserEndpoints[F[_]: Effect, A, D[_]](
    userService: UserService[F, D],
    cryptService: PasswordHasher[F, A]
) extends Http4sDsl[F] {

  implicit val userUpdateDecoder: EntityDecoder[F, command.UpdateUser] = jsonOf
  implicit val userCreateDecoder: EntityDecoder[F, command.CreateUser] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf
  implicit val signupReqDecoder: EntityDecoder[F, SignupRequest] = jsonOf

  def endpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root                         => signup(req)
      case req @ PUT -> Root / name                   => update(req, name)
      case GET -> Root :? PageSizeQ(ps) :? OffsetQ(o) => list(ps, o)
      case GET -> Root / username                     => searchByUsername(username)
      case DELETE -> Root / username                  => deleteByUsername(username)
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
      user <- req.as[command.UpdateUser]
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
  def endpoints[F[_]: Effect, A, D[_]](
      userService: UserService[F, D],
      cryptService: PasswordHasher[F, A]
  ): HttpRoutes[F] =
    new UserEndpoints[F, A, D](userService, cryptService).endpoints
}
