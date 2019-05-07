package org.codecannery.lunchplanner.infrastructure.endpoint

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import org.http4s.CacheDirective._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Cache-Control`
import org.http4s.server.HttpMiddleware
import org.http4s.server.middleware._

import scala.concurrent.ExecutionContext.global
import scala.language.higherKinds

class StaticEndpoints[F[_]: Effect: ContextShift, D[_], H] extends Http4sDsl[F] {

  object dsl extends Http4sDsl[F]

  val middlewareWrapper: HttpMiddleware[F] =
  { service: HttpRoutes[F] => AutoSlash(service) } compose
  { service: HttpRoutes[F] => GZip(service)      }

  private val supportedStaticExtensions =
    List(".html", ".js", ".map", ".css", ".png", ".ico", "/")

  def nonAuthEndpoints =
    HttpRoutes.of[F] {
      case req if supportedStaticExtensions.exists(req.pathInfo.endsWith) || !req.pathInfo.contains(".") =>
        val isItStatic = req.pathInfo.startsWith("/static")
        val pullFromPath = if (isItStatic) req.pathInfo else "/index.html"

        StaticFile.fromResource(pullFromPath, global, Some(req)).getOrElseF(NotFound())
          .map(_.putHeaders(`Cache-Control`(NonEmptyList.of(`no-cache`()))))
    }

}


object StaticEndpoints {
  def endpoints[F[_]: Effect: ContextShift, D[_], H]() = {
    val staticEndpoints = new StaticEndpoints[F, D, H]

    staticEndpoints.middlewareWrapper(staticEndpoints.nonAuthEndpoints)
  }
}