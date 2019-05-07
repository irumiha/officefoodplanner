package com.officefoodplanner.infrastructure.endpoint

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class UtilityEndpoints[F[_]: Effect, D[_], H] extends Http4sDsl[F] {

  case class ClassPathList(cp: List[String])

  def nonAuthEndpoints: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ GET -> Root / "classpath" => showClasspath(req)
    }

  private def showClasspath(req: Request[F]): F[Response[F]] = {
    val cp = Option(System.getProperty("java.class.path")).map(_.split(":"))

    cp match {
      case Some(cs) => Ok(ClassPathList(cs.toList).asJson)
      case None     => NotFound()
    }
  }

}

object UtilityEndpoints {
  def endpoints[F[_]: Effect, D[_], H](): HttpRoutes[F] = {
    val utilityEndpoints = new UtilityEndpoints[F, D, H]

    utilityEndpoints.nonAuthEndpoints
  }
}
