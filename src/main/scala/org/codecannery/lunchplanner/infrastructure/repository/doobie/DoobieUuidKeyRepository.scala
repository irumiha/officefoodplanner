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
  def insertMany(table: Table, rows: List[RowWrapper]): Query0[RowWrapper] = {
      sql"""INSERT INTO "${table.schemaName.v}"."${table.tableName.v}" (ID, DATA, CREATED_ON)
            VALUES (unnest(${rows.map(_.id)}), unnest(${rows.map(_.data)}), unnest(${rows.map(_.createdOn)}))
            RETURNING *""".query[RowWrapper]
  }

  def updateMany(table: Table, rows: List[RowWrapper]): Update0 = {
    sql"""UPDATE "${table.schemaName.v}"."${table.tableName.v}"
            SET DATA = data_table.data,
                UPDATED_ON = data_table.updated_on
           FROM (
             select unnest(${rows.map(_.id)}) as key, unnest(${rows.map(_.data)}) as data, unnest(${rows.map(_.updatedOn)}) as updated_on
           ) as data_table
           where "${table.schemaName.v}"."${table.tableName.v}".ID = data_table.key""".update
  }
}

abstract class DoobieUuidKeyJsonRepository[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity]
    extends Repository[ConnectionIO, UUID, E]
    with DatabaseRepository {
  import DoobieUuidKeyJsonRepositorySQL._

  override def table: Table

  override def create(entity: E): doobie.ConnectionIO[E] = {
    val row = entityToRowWrapper(entity)
    insertMany(table, List(row)).map(toEntity).unique
  }

  override def create(entities: Seq[E]): doobie.ConnectionIO[Seq[E]] = {
    val rows = entities.map(entityToRowWrapper).toList
    insertMany(table, rows).map(toEntity).to[Seq]
  }

  override def update(entity: E): doobie.ConnectionIO[Int] = {
    val row = entityToRowWrapper(entity)
    updateMany(table, List(row)).run
  }

  override def update(entities: Seq[E]): doobie.ConnectionIO[Int] = {
    updateMany(table, entities.map(entityToRowWrapper).toList).run
  }

  override def get(entityId: UUID): doobie.ConnectionIO[Option[E]] = ???

  override def get(entityIds: Seq[UUID]): doobie.ConnectionIO[Seq[E]] = ???

  override def delete(entityId: UUID): doobie.ConnectionIO[Int] = ???

  override def delete(entityIds: Seq[UUID]): doobie.ConnectionIO[Int] = ???

  override def deleteEntity(entity: E): doobie.ConnectionIO[Int] = ???

  override def deleteEntities(entities: Seq[E]): doobie.ConnectionIO[Int] = ???

  override protected def find(specification: String, orderBy: Option[String], pageSize: Option[Int], offset: Option[Int]): doobie.ConnectionIO[Seq[E]] = ???

  def entityToRowWrapper(entity: E): RowWrapper = {
    RowWrapper(
      data = Encoder[E].apply(entity).noSpaces,
      createdOn = ZonedDateTime.now(),
      updatedOn = ZonedDateTime.now(),
      id = UuidKeyEntity[E].key(entity),
    )
  }

  def toEntity(rw: RowWrapper): E = {
    decode[E](rw.data).right.get
  }

}
