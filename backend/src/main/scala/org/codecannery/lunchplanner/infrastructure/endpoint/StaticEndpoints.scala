package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.data.{NonEmptyList, OptionT}
import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Cache-Control`
import org.http4s.CacheDirective._
import scala.concurrent.ExecutionContext.global
import org.http4s.implicits._

import scala.language.higherKinds

class StaticEndpoints[F[_]: Effect: ContextShift, D[_], H] extends Http4sDsl[F] {

  object dsl extends Http4sDsl[F]
  import dsl._

  private val supportedStaticExtensions =
    List(".html", ".js", ".map", ".css", ".png", ".ico")

  private def getResource(pathInfo: String) = Effect[F].delay(getClass.getResource(pathInfo))

  def nonAuthEndpoints =
    HttpRoutes.of[F] {
      case req @ GET -> Root / path if supportedStaticExtensions.exists(path.endsWith) =>
        StaticFile.fromResource("/" + path, global, Some(req)).getOrElseF(NotFound())
          .map(_.putHeaders(`Cache-Control`(NonEmptyList.of(`no-cache`()))))
    }

}


object StaticEndpoints {
  def endpoints[F[_]: Effect: ContextShift, D[_], H]() = {
    val staticEndpoints = new StaticEndpoints[F, D, H]

    staticEndpoints.nonAuthEndpoints
  }
}
