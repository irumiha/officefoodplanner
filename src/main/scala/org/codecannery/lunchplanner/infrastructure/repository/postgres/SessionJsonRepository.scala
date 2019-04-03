package org.codecannery.lunchplanner.infrastructure.repository.postgres

import doobie.ConnectionIO
import org.codecannery.lunchplanner.domain.authentication.SessionRepository
import org.codecannery.lunchplanner.domain.authentication.model.Session
import org.codecannery.lunchplanner.infrastructure.repository.{Table, TableName}

class SessionJsonRepository extends JsonRepository[Session] with SessionRepository[ConnectionIO] {
  override def table: Table = Table(tableName = TableName("sessions"))
}
