package org.codecannery.lunchplanner.infrastructure.service

abstract class TransactingService[F[_], D[_]] {
  def transact[A](t: D[A]): F[A]
}
