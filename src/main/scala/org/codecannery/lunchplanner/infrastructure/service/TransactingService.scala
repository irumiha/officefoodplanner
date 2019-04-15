package org.codecannery.lunchplanner.infrastructure.service

trait TransactingService[F[_], D[_]] {
  def transact[A](t: D[A]): F[A]
}
