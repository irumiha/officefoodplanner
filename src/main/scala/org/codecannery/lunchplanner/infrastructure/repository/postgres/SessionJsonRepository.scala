package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie.ConnectionIO
import org.codecannery.lunchplanner.domain.authentication.SessionRepository
import org.codecannery.lunchplanner.domain.authentication.model.Session
import org.codecannery.lunchplanner.infrastructure.repository.{Table, TableName}

class SessionJsonRepository extends SessionRepository[ConnectionIO] {
  private def repo = new JsonRepository[Session] {
    override def table: Table = Table(tableName = TableName("sessions"))
  }

  def create(session: Session): doobie.ConnectionIO[Session] = repo.create(session)

  def get(sessionID: UUID): ConnectionIO[Option[Session]] = repo.get(sessionID)

  def update(session: Session): ConnectionIO[Int] = repo.update(session)

}
