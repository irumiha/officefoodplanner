package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.util.UUID

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.codecannery.lunchplanner.domain.authentication.SessionRepository
import org.codecannery.lunchplanner.domain.authentication.model.Session
import org.codecannery.lunchplanner.infrastructure.repository.{DoobieColumn, DoobieSupport, Table, TableName}

class SessionTableRepository extends SessionRepository[ConnectionIO] {
  import SessionTableRepository.doobieSupport

  private def repo = new TableRepository[Session, UUID] {
    override def table: Table = Table(tableName = TableName("sessions"))
  }

  def create(session: Session): doobie.ConnectionIO[Session] = repo.create(session)

  def get(sessionID: UUID): ConnectionIO[Option[Session]] = repo.get(sessionID)

  def update(session: Session): ConnectionIO[Int] = repo.update(session)

}

object SessionTableRepository {
  implicit val doobieSupport: DoobieSupport[Session] = new DoobieSupport[Session] {
    override val id: DoobieColumn[Session] = DoobieColumn[Session]("id", s => fr0"${s.id}")

    override val columns: List[DoobieColumn[Session]] = List(
      DoobieColumn[Session]("id", e => fr0"${e.id}"),
      DoobieColumn[Session]("user_id", e => fr0"${e.userID}"),
      DoobieColumn[Session]("created_on", e => fr0"${e.createdOn}"),
      DoobieColumn[Session]("expires_on", e => fr0"${e.expiresOn}"),
      DoobieColumn[Session]("updated_on", e => fr0"${e.updatedOn}"),
    )
  }
}
