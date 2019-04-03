package org.codecannery.lunchplanner.domain.authentication

import java.util.UUID

import org.codecannery.lunchplanner.domain.authentication.model.Session

trait SessionRepository[F[_]] {
  def create(session: Session): F[Session]

  def get(sessionID: UUID): F[Option[Session]]
}
