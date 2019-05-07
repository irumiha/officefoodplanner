package com.officefoodplanner.infrastructure.service

trait TransactingService[F[_], D[_]] {
  def transact[A](t: D[A]): F[A]
}
