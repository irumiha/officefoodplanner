package org.codecannery.lunchplanner.infrastructure.repository.postgres

import java.sql.{Timestamp => JTimestamp}
import java.time.Instant
import java.util.UUID

import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.postgres.circe.jsonb.implicits._
import doobie.util.fragment
import doobie.util.query.Query0
import io.circe._
import org.codecannery.lunchplanner.infrastructure.repository._

private case class RowWrapper(id: UUID, data: Json, createdOn: JTimestamp, updatedOn: JTimestamp)

private object JsonRepositorySQL {

  def fromEntity[E: Encoder: Decoder: UuidKeyEntity](entity: E): RowWrapper = {
    val now = Instant.now()
    RowWrapper(
      data = Encoder[E].apply(entity),
      createdOn = JTimestamp.from(now),
      updatedOn = JTimestamp.from(now),
      id = UuidKeyEntity[E].key(entity),
    )
  }

  def toEntity[E: Encoder: Decoder: UuidKeyEntity](rw: RowWrapper): E =
    rw.data.as[E].right.get

  def tableFragment(table: Table): fragment.Fragment =
    Fragment.const(s""""${table.schemaName.v}"."${table.tableName.v}"""")

  def columnFragment(table: Table, columnName: String): fragment.Fragment =
    Fragment.const(s""""${table.schemaName.v}"."${table.tableName.v}"."$columnName"""")

  def insertOne(table: Table, row: RowWrapper): Update0 = {
    val sql = fr"""INSERT INTO """ ++
      tableFragment(table) ++
      fr""" (ID, DATA, CREATED_ON, UPDATED_ON) VALUES (${row.id}, ${row.data}, ${row.createdOn}, ${row.updatedOn})"""

    sql.update
  }

  def insertMany(table: Table): Update[RowWrapper] = {

    val insertSQL =
      s"""INSERT INTO "${table.schemaName.v}"."${table.tableName.v}" (ID, DATA, CREATED_ON, UPDATED_ON) VALUES (?, ?, ?, ?)"""

    Update[RowWrapper](insertSQL)
  }

  def updateOne(table: Table, row: RowWrapper): Update0 = {
    val sql = fr"""UPDATE""" ++
      tableFragment(table) ++
      fr"""SET DATA = ${row.data}, UPDATED_ON=${row.updatedOn} WHERE ID = ${row.id}"""
    sql.update
  }

  def updateMany(table: Table): Update[RowWrapper] = {

    val updateSQL =
      s"""UPDATE "${table.schemaName.v}"."${table.tableName.v}" SET DATA = ?, UPDATED_ON = ? WHERE ID = ?"""

    Update[RowWrapper](updateSQL)
  }

  def deleteManyIDs(table: Table, rows: List[UUID]): Update0 = {
    import Fragments.{in, whereAndOpt}
    import cats.syntax.list._

    val q =
      fr"""DELETE FROM """ ++ tableFragment(table) ++
        whereAndOpt(rows.toNel.map(r => in(fr"ID", r)))

    q.update
  }

  def getMany(table: Table, ids: List[UUID]): Query0[RowWrapper] = {
    import Fragments.{in, whereAndOpt}
    import cats.syntax.list._

    val q =
      fr"""SELECT ID, DATA, CREATED_ON, UPDATED_ON from """ ++
        tableFragment(table) ++
        whereAndOpt(ids.toNel.map(r => in(fr"ID", r)))

    q.query[RowWrapper]
  }

  def whereAnd(fs: Fragment*): Fragment = {
    import Fragments.and
    if (fs.forall(_ == Fragment.empty)) Fragment.empty else fr"WHERE" ++ and(fs: _*)
  }

  def select(
      table: Table,
      where: Fragment,
      orderBy: Fragment,
      offset: Fragment,
      limit: Fragment): Query0[RowWrapper] = {

    val q =
      fr"""SELECT ID, DATA, CREATED_ON, UPDATED_ON from""" ++
        tableFragment(table) ++
        whereAnd(where) ++
        orderBy ++
        offset ++
        limit

    q.query[RowWrapper]
  }

  def deleteBySpecification(
      table: Table,
      where: Fragment
  ): Query0[RowWrapper] = {
    val q =
      fr"""DELETE FROM""" ++
        tableFragment(table) ++
        whereAnd(where) ++
        fr"""RETURNING *"""

    q.query[RowWrapper]
  }
}

abstract class JsonRepository[E: Encoder: Decoder: UuidKeyEntity]
    extends Repository[ConnectionIO, UUID, E, Fragment]
    with FindRepository[ConnectionIO, UUID, E, Fragment, Fragment]
    with DatabaseRepository {
  import JsonRepositorySQL._

  override def create(entity: E): ConnectionIO[E] = {
    val row = fromEntity(entity)

    insertOne(table, row)
      .withUniqueGeneratedKeys[RowWrapper]("id", "data", "created_on", "updated_on")
      .map(toEntity[E])
  }

  override def create(entities: List[E]): ConnectionIO[List[E]] = {
    val rows = entities.map(fromEntity[E])
    insertMany(table)
      .updateManyWithGeneratedKeys[RowWrapper]("id", "data", "created_on", "updated_on")(rows)
      .map(toEntity[E])
      .compile
      .toList
  }

  override def update(entity: E): ConnectionIO[Int] = {
    val row = fromEntity(entity)
    updateOne(table, row).run
  }

  override def update(entities: List[E]): ConnectionIO[Int] =
    updateMany(table).updateMany(entities.map(fromEntity[E]))

  override def get(entityId: UUID): ConnectionIO[Option[E]] =
    getMany(table, List(entityId)).map(toEntity[E]).option

  override def get(entityIds: List[UUID]): ConnectionIO[List[E]] =
    getMany(table, entityIds).map(toEntity[E]).to[List]

  override def deleteById(entityId: UUID): ConnectionIO[Int] =
    deleteManyIDs(table, List(entityId)).run

  override def deleteByIds(entityIds: List[UUID]): ConnectionIO[Int] =
    deleteManyIDs(table, entityIds).run

  override def deleteEntity(entity: E): ConnectionIO[Int] =
    deleteManyIDs(table, List(UuidKeyEntity[E].key(entity))).run

  override def deleteEntities(entities: List[E]): ConnectionIO[Int] =
    deleteManyIDs(table, entities.map(entity => UuidKeyEntity[E].key(entity))).run

  override def delete(specification: Fragment): ConnectionIO[List[E]] =
    deleteBySpecification(
      table = table,
      where = specification
    ).map(toEntity[E]).to[List]

  override def find(specification: Fragment, orderBy: Fragment, limit: Int, offset: Int): ConnectionIO[List[E]] =
    select(
      table = table,
      where = specification,
      orderBy = orderBy,
      offset = Fragment.const(s"$offset"),
      limit = Fragment.const(s"$limit"),
    ).map(toEntity[E]).to[List]

  override def listAll: doobie.ConnectionIO[List[E]] =
    find(Fragment.empty, Fragment.empty, 0, Int.MaxValue)
}
