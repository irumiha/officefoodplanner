package org.codecannery.lunchplanner.infrastructure.repository.doobie

import java.time.ZonedDateTime
import java.util.UUID

import cats._
import doobie._
import doobie.implicits._
import io.circe._
import io.circe.parser.decode
import org.codecannery.lunchplanner.domain.{DatabaseRepository, Repository, Table, TableName}
import org.codecannery.lunchplanner.infrastructure.repository.UuidKeyEntity

private case class RowWrapper(data: String, createdOn: ZonedDateTime, updatedOn: ZonedDateTime, id: UUID)

private object DoobieUuidKeyJsonRepositorySQL {
  def insert(table: Table, row: RowWrapper): Query0[RowWrapper] = sql"""
    INSERT INTO "${table.schemaName.v}"."${table.tableName.v}" (ID, DATA, CREATED_ON)
    VALUES (${row.id}, ${row.data}, ${row.createdOn})
    RETURNING *
  """.query[RowWrapper]

  def insertMany(table: Table, rows: List[RowWrapper]): Query0[RowWrapper] = {
    import cats.implicits._

    val init = fr"""INSERT INTO "${table.schemaName.v}"."${table.tableName.v}" (ID, DATA, CREATED_ON) VALUES"""

    val values = rows.map(row => fr"""(${row.id}, ${row.data}, ${row.createdOn}, NULL)""").intercalate(fr",")

    val returning = fr"""RETURNING *"""

    (init ++ values ++ returning).query[RowWrapper]
  }

}

class DoobieUuidKeyJsonRepository[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity]
    extends Repository[ConnectionIO, UUID, E]
    with DatabaseRepository {
  import DoobieUuidKeyJsonRepositorySQL._

  override def table: Table = Table(tableName = TableName("users"))

  override def create(entity: E): doobie.ConnectionIO[E] = {
    val row = entityToRowWrapper(entity)
    insert(table, row).map(toEntity).unique
  }

  override def create(entities: Seq[E]): doobie.ConnectionIO[Seq[E]] = {
    val rows = entities.map(entityToRowWrapper).toList
    insertMany(table, rows).map(toEntity).to[Seq]
  }

  override def update(entity: E): doobie.ConnectionIO[Int] = ???

  override def update(entities: Seq[E]): doobie.ConnectionIO[Int] = ???

  override def get(entityId: UUID): doobie.ConnectionIO[Option[E]] = ???

  override def get(entityIds: Seq[UUID]): doobie.ConnectionIO[Seq[E]] = ???

  override def delete(entityId: UUID): doobie.ConnectionIO[Int] = ???

  override def delete(entityIds: Seq[UUID]): doobie.ConnectionIO[Int] = ???

  override def deleteEntity(entity: E): doobie.ConnectionIO[Int] = ???

  override def deleteEntities(entities: Seq[E]): doobie.ConnectionIO[Int] = ???

  override protected def find(specification: String, orderBy: Option[String], pageSize: Option[Int], offset: Option[Int]): doobie.ConnectionIO[Seq[E]] = ???

  private def entityToRowWrapper(entity: E): RowWrapper = {
    RowWrapper(
      data = Encoder[E].apply(entity).noSpaces,
      createdOn = ZonedDateTime.now(),
      updatedOn = ZonedDateTime.now(),
      id = UuidKeyEntity[E].key(entity),
    )
  }

  private def toEntity(rw: RowWrapper): E = {
    decode[E](rw.data).right.get
  }

}
