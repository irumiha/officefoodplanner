package org.codecannery.lunchplanner.infrastructure.repository.doobie

import java.time.ZonedDateTime
import java.util.UUID

import cats._
import doobie._
import doobie.implicits._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import org.codecannery.lunchplanner.domain.Repository
import org.codecannery.lunchplanner.infrastructure.repository.{Table, TableEntity, UuidKeyEntity}

private case class RowWrapper(data: String, createdOn: ZonedDateTime, id: UUID)

private object UuidKeyJsonRepositorySQL {
  def insert(table: Table, row: RowWrapper): Query0[RowWrapper]= sql"""
    INSERT INTO "${table.schemaName.v}"."${table.tableName.v}" (ID, DATA, CREATED_ON)
    VALUES (${row.id}, ${row.data}, ${row.createdOn})
    RETURNING *
  """.query[RowWrapper]

}

class DoobieUuidKeyJsonRepositoryInterpreter[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity: TableEntity](
    val xa: Transactor[F])
    extends Repository[F, UUID, E] {
  import UuidKeyJsonRepositorySQL._

  override def create(entity: E): F[E] = {
    val row = RowWrapper(
      data      = Encoder[E].apply(entity).noSpaces,
      createdOn = ZonedDateTime.now(),
      id        = UuidKeyEntity[E].key(entity),
    )
    insert(TableEntity[E].table(entity), row).map(r => Decoder[E].apply()).transact(xa)
  }

  override def create(entities: Seq[E]): F[Seq[E]] = ???

  override def update(entity: E): F[Option[E]] = ???

  override def update(entities: Seq[E]): F[Seq[E]] = ???

  override def get(entityId: UUID): F[Option[E]] = ???

  override def get(entityIds: Seq[UUID]): F[Seq[E]] = ???

  override def delete(entityId: UUID): F[Option[E]] = ???

  override def delete(entityIds: Seq[UUID]): F[Seq[E]] = ???

  override def deleteEntity(entity: E): F[Option[E]] = ???

  override def deleteEntities(entities: Seq[E]): F[Seq[E]] = ???

  override protected def find(specification: String,
                              orderBy: Option[String],
                              pageSize: Option[Int],
                              offset: Option[Int]): F[Seq[E]] = ???
}

object DoobieUuidKeyRepositoryInterpreter {
  def apply[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity](
      xa: Transactor[F]): DoobieUuidKeyJsonRepositoryInterpreter[F, E] =
    new DoobieUuidKeyJsonRepositoryInterpreter(xa)
}
