package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats.Applicative
import com.officefoodplanner.domain.auth.model.Session
import com.officefoodplanner.domain.auth.repository.SessionRepository

class SessionInMemoryRepository[F[_] : Applicative] extends SessionRepository[F] {
  private val repo = new InMemoryRepository[F, Session, UUID]

  def create(session: Session): F[Session] = repo.create(session)

  def get(sessionID: UUID): F[Option[Session]] = repo.get(sessionID)

  def update(session: Session): F[Int] = repo.update(session)

}
