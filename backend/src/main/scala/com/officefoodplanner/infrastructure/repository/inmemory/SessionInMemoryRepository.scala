package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats.Applicative
import com.officefoodplanner.domain.auth.SessionRepository
import com.officefoodplanner.domain.auth.model.Session

class SessionInMemoryRepository[F[_] : Applicative] extends SessionRepository[F] {
  private def repo = new InMemoryRepository[F, Session, UUID]

  def create(session: Session): F[Session] = repo.create(session)

  def get(sessionID: UUID): F[Option[Session]] = repo.get(sessionID)

  def update(session: Session): F[Int] = repo.update(session)

}
