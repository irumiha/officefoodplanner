package org.codecannery.lunchplanner.infrastructure.repository.doobie

import java.time.ZonedDateTime
import java.util.UUID

import cats._
import doobie._
import doobie.implicits._
import io.circe._
import org.codecannery.lunchplanner.domain.Repository
import org.codecannery.lunchplanner.infrastructure.repository.{Table, TableEntity, UuidKeyEntity}

private case class RowWrapper(data: String, createdOn: ZonedDateTime, id: UUID)

private object DoobieUuidKeyJsonRepositorySQL {
  def insert(table: Table, row: RowWrapper): Update0 = sql"""
    INSERT INTO "${table.schemaName.v}"."${table.tableName.v}" (ID, DATA, CREATED_ON)
    VALUES (${row.id}, ${row.data}, ${row.createdOn})
  """.update
}

class DoobieUuidKeyJsonRepository[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity: TableEntity](
    val xa: Transactor[F])
    extends Repository[F, UUID, E] {
  import DoobieUuidKeyJsonRepositorySQL._

  override def create(entity: E): F[Int] = {
    val row = RowWrapper(
      data      = Encoder[E].apply(entity).noSpaces,
      createdOn = ZonedDateTime.now(),
      id        = UuidKeyEntity[E].key(entity),
    )
    insert(TableEntity[E].table(entity), row).run.transact(xa)
  }

  override def create(entities: Seq[E]): F[Int] = ???

  override def update(entity: E): F[Int] = ???

  override def update(entities: Seq[E]): F[Int] = ???

  override def get(entityId: UUID): F[Option[E]] = ???

  override def get(entityIds: Seq[UUID]): F[Seq[E]] = ???

  override def delete(entityId: UUID): F[Int] = ???

  override def delete(entityIds: Seq[UUID]): F[Int] = ???

  override def deleteEntity(entity: E): F[Int] = ???

  override def deleteEntities(entities: Seq[E]): F[Int] = ???

  override protected def find(specification: String,
                              orderBy: Option[String],
                              pageSize: Option[Int],
                              offset: Option[Int]): F[Seq[E]] = ???
}

object DoobieUuidKeyRepositoryInterpreter {
  def apply[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity: TableEntity](
      xa: Transactor[F]): DoobieUuidKeyJsonRepository[F, E] =
    new DoobieUuidKeyJsonRepository(xa)
}
