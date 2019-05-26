package com.officefoodplanner.domain.auth.repository

import java.util.UUID

import com.officefoodplanner.domain.auth.model.Session

trait SessionRepository[F[_]] {
  def create(session: Session): F[Session]

  def get(sessionID: UUID): F[Option[Session]]

  def update(session: Session): F[Int]
}
