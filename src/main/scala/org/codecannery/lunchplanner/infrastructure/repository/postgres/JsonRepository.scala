package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.time.{Instant, ZonedDateTime}
import java.util.UUID

import cats._
import cats.data._
import cats.implicits._
import cats.syntax.list._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragment
import io.circe._
import io.circe.parser.decode
import org.codecannery.lunchplanner.infrastructure.repository.{DatabaseRepository, Limit, Offset, OrderBy, Repository, Specification, Table, UuidKeyEntity}

private case class RowWrapper(data: String, createdOn: Instant, updatedOn: Instant, id: UUID)

private object JsonRepositorySQL {
  import FragmentsExtra._

  def insertMany(table: Table, rows: List[RowWrapper]): Query0[RowWrapper] = {

    val insert =
      fr"""INSERT INTO """ ++ tableFragment(table) ++ fr""" (ID, DATA, CREATED_ON)"""
    val values = valuesUnnest(rows.map(_.id), rows.map(_.data), rows.map(_.createdOn))
    val returning = fr"""RETURNING *"""

    (insert ++ values ++ returning).query[RowWrapper]
  }

  private def tableFragment(table: Table): fragment.Fragment =
    Fragment.const(s""""${table.schemaName.v}"."${table.tableName.v}"""")

  def updateMany(table: Table, rows: List[RowWrapper]): Update0 = {
    val update = fr"""UPDATE """ ++ tableFragment(table) ++
      fr"""SET DATA = data_table.data,
                 UPDATED_ON = data_table.updated_on"""

    val values = fromUnnest(
      rows.map(_.id),
      "key",
      rows.map(_.data),
      "data",
      rows.map(_.updatedOn),
      "updated_on"
    ) ++ fr" as data_table"
    val closing = fr"""where "${table.schemaName.v}"."${table.tableName.v}".ID = data_table.key"""

    (update ++ values ++ closing).update
  }

  def deleteManyIDs(table: Table, rows: List[UUID]): Update0 = {
    import Fragments.{in, whereAndOpt}

    val q =
      fr"""DELETE FROM """ ++ tableFragment(table) ++ fr""" WHERE""" ++
        whereAndOpt(rows.toNel.map(r => in(fr"ID", r)))

    q.update
  }

  def getMany(table: Table, ids: List[UUID]): Query0[RowWrapper] = {
    import Fragments.{in, whereAndOpt}

    val q =
      fr"""SELECT ID, DATA, CREATED_ON, UPDATED_ON from """ ++ tableFragment(table) ++
        whereAndOpt(ids.toNel.map(r => in(fr"ID", r)))

    q.query[RowWrapper]
  }

  def filter(table: Table, where: Fragment, orderBy: Fragment, offset: Fragment, limit: Fragment): Query0[RowWrapper] = {

    val q =
      fr"""SELECT ID, DATA, CREATED_ON, UPDATED_ON from """ ++
        tableFragment(table) ++
        where ++
        orderBy ++
        offset ++
        limit

    q.query[RowWrapper]
  }
}

abstract class JsonRepository[F[_]: Monad, E: Encoder: Decoder: UuidKeyEntity]
    extends Repository[ConnectionIO, UUID, E]
    with DatabaseRepository {
  import JsonRepositorySQL._

  override def table: Table

  override def create(entity: E): doobie.ConnectionIO[E] = {
    val row = entityToRowWrapper(entity)
    insertMany(table, List(row)).map(toEntity).unique
  }

  override def create(entities: List[E]): doobie.ConnectionIO[List[E]] = {
    val rows = entities.map(entityToRowWrapper).toList
    insertMany(table, rows).map(toEntity).to[List]
  }

  override def update(entity: E): doobie.ConnectionIO[Int] = {
    val row = entityToRowWrapper(entity)
    updateMany(table, List(row)).run
  }

  override def update(entities: List[E]): doobie.ConnectionIO[Int] =
    updateMany(table, entities.map(entityToRowWrapper).toList).run

  override def get(entityId: UUID): doobie.ConnectionIO[Option[E]] = {
    getMany(table, List(entityId)).map(toEntity).option
  }

  override def get(entityIds: List[UUID]): doobie.ConnectionIO[List[E]] = {
    getMany(table, entityIds).map(toEntity).to[List]
  }

  override def delete(entityId: UUID): doobie.ConnectionIO[Int] =
    deleteManyIDs(table, List(entityId)).run

  override def delete(entityIds: List[UUID]): doobie.ConnectionIO[Int] =
    deleteManyIDs(table, entityIds.toList).run

  override def deleteEntity(entity: E): doobie.ConnectionIO[Int] =
    deleteManyIDs(table, List(UuidKeyEntity[E].key(entity))).run

  override def deleteEntities(entities: List[E]): doobie.ConnectionIO[Int] =
    deleteManyIDs(table, entities.map(entity => UuidKeyEntity[E].key(entity)).toList).run


  override protected def find(specification: Specification): doobie.ConnectionIO[List[E]] = ???

  override protected def find(specification: Specification, orderBy: OrderBy): doobie.ConnectionIO[List[E]] = ???

  override protected def find(specification: Specification, limit: Limit): doobie.ConnectionIO[List[E]] = ???

  override protected def find(specification: Specification, limit: Limit, offset: Offset): doobie.ConnectionIO[List[E]] = ???

  override protected def find(specification: Specification, orderByFragment: OrderBy, limit: Limit, offset: Offset): doobie.ConnectionIO[List[E]] = ???

  def entityToRowWrapper(entity: E): RowWrapper =
    RowWrapper(
      data = Encoder[E].apply(entity).noSpaces,
      createdOn = Instant.now(),
      updatedOn = Instant.now(),
      id = UuidKeyEntity[E].key(entity),
    )

  def toEntity(rw: RowWrapper): E =
    decode[E](rw.data).right.get

}
