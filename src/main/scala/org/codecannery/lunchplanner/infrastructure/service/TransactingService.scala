package org.codecannery.lunchplanner.infrastructure.service

import cats._

abstract class TransactingService[F[_]: Monad, D[_]: Monad] {
  def transact[A](t: D[A]): F[A]
}
