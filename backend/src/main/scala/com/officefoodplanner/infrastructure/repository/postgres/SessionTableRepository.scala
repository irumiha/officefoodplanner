package com.officefoodplanner.infrastructure.repository.postgres

import java.util.UUID
import com.officefoodplanner.domain.auth.model.Session
import com.officefoodplanner.domain.auth.repository.SessionRepository
import com.officefoodplanner.infrastructure.repository._
import doobie._
import doobie.postgres.implicits._
import net.liftio.persistence.doobie.postgres.TableDao

object SessionTableRepository extends SessionRepository[ConnectionIO] {
  private val table = Table(SchemaName("auth"), TableName("sessions"))
  private val dao: TableDao.Aux[Session, UUID] =
    TableDao.make[Session](TableDao.derive[Session, UUID]("id", table))

  def create(session: Session): doobie.ConnectionIO[Session] = dao.create(session)

  def get(sessionID: UUID): ConnectionIO[Option[Session]] = dao.get(sessionID)

  def update(session: Session): ConnectionIO[Int] = dao.update(session)

}
