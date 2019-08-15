package com.officefoodplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats.Applicative
import com.officefoodplanner.domain.auth.model.Session
import com.officefoodplanner.domain.auth.repository.SessionRepository

class SessionInMemoryRepository[F[_] : Applicative] extends SessionRepository[F] {
  private val dao: InMemoryDao.Aux[F, Session, UUID] =
    InMemoryDao[F, Session](InMemoryDao.derive[F, Session, UUID].apply(_.id))
  
  def create(session: Session): F[Session] = dao.create(session)

  def get(sessionID: UUID): F[Option[Session]] = dao.get(sessionID)

  def update(session: Session): F[Int] = dao.update(session)

}
