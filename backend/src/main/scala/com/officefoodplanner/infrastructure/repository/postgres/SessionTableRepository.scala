package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID

import com.officefoodplanner.domain.auth.model.Session
import com.officefoodplanner.domain.auth.repository.SessionRepository
import com.officefoodplanner.infrastructure.repository.{DoobieColumn, DoobieSupport, SchemaName, Table, TableName}
import doobie._
import doobie.postgres.implicits._

class SessionTableRepository extends SessionRepository[ConnectionIO] {
  import SessionTableRepository.doobieSupport

  private def repo = new TableRepository[Session, UUID] {
    override def table: Table = Table(schemaName = SchemaName("auth"), tableName = TableName("sessions"))
  }

  def create(session: Session): doobie.ConnectionIO[Session] = repo.create(session)

  def get(sessionID: UUID): ConnectionIO[Option[Session]] = repo.get(sessionID)

  def update(session: Session): ConnectionIO[Int] = repo.update(session)

}

object SessionTableRepository {
  implicit val doobieSupport: DoobieSupport[Session] = new DoobieSupport[Session] {
    override val id: DoobieColumn[Session] = DoobieColumn("id")(_.id)

    override val columns = List(
      id,
      DoobieColumn("user_id"   )((p: Session) => p.userID),
      DoobieColumn("created_on")((p: Session) => p.createdOn),
      DoobieColumn("expires_on")((p: Session) => p.expiresOn),
      DoobieColumn("updated_on")((p: Session) => p.updatedOn),
    )
  }
}
