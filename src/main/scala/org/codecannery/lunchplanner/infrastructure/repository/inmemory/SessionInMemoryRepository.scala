package org.codecannery.lunchplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats.Applicative
import org.codecannery.lunchplanner.domain.authentication.SessionRepository
import org.codecannery.lunchplanner.domain.authentication.model.Session

class SessionInMemoryRepository[F[_] : Applicative] extends SessionRepository[F] {
  private def repo = new InMemoryUUIDKeyRepository[F, Session]

  def create(session: Session): F[Session] = repo.create(session)

  def get(sessionID: UUID): F[Option[Session]] = repo.get(sessionID)

  def update(session: Session): F[Int] = repo.update(session)

}
