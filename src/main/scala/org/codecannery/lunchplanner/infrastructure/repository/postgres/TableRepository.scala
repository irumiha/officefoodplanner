package org.codecannery.lunchplanner.infrastructure.repository.postgres

import cats.implicits._
import doobie._, doobie.implicits._
import doobie.util.query.Query0
import org.codecannery.lunchplanner.infrastructure.repository._

private object TableRepositorySQL {

  private def tableName(table: Table) =
    s""""${table.schemaName.v}"."${table.tableName.v}""""

  private def columnName(table: Table, column: String) =
    s""""$column""""

  private def tableNameFragment(table: Table) = Fragment.const(tableName(table))

  private def columnNameFragment(table: Table, column: String) = Fragment.const(columnName(table, column))

  def tableColumns[E: DoobieColumnList](table: Table): Fragment =
    DoobieColumnList[E].columns.map(columnNameFragment(table, _)).intercalate(fr",")

  def insertOne[E: DoobieColumnList : DoobieColumnValues](table: Table, row: E): Update0 = {
    val sql = fr"INSERT INTO " ++
      tableNameFragment(table) ++
      fr0"(" ++ tableColumns(table) ++ fr")" ++
      fr"VALUES (" ++ DoobieColumnValues[E].values(row).intercalate(fr",") ++ fr")"

    sql.update
  }

  def insertMany[E: DoobieColumnList : Write](table: Table): Update[E] = {
    val insertSQL =
      s"""INSERT INTO ${tableName(table)}
         (${DoobieColumnList[E].columns.mkString(", ")})
         VALUES (${DoobieColumnList[E].columns.map(_ => "?").mkString(", ")})"""

    Update[E](insertSQL)
  }

  def updateOne[E: DoobieColumnList : DoobieColumnValues](table: Table, row: E): Update0 = {
    val sql =
      fr"""UPDATE""" ++
        tableNameFragment(table) ++
        fr"SET" ++
        DoobieColumnList[E].columns.map(c => Fragment.const(columnName(table, c)))
          .zip(DoobieColumnValues[E].values(row))
          .map { case (c, v) => c ++ Fragment.const("=") ++ v }
          .intercalate(fr",")

    sql.update
  }

  def updateMany[E: DoobieColumnList : DoobieIDColumn : Write, K: Write](table: Table): Update[(E, K)] = {
    val columnPlaceholders = DoobieColumnList[E].columns.map(c => s"${columnName(table, c)} = ?").mkString(", ")
    val updateSQL =
      s"""UPDATE "${table.schemaName.v}"."${table.tableName.v}" SET $columnPlaceholders WHERE ${columnName(table, DoobieIDColumn[E].id)} = ?"""

    Update[(E, K)](updateSQL)
  }

  def deleteManyIDs[E: DoobieIDColumn, K: Put](table: Table, rows: List[K]): Update0 = {
    import Fragments.{in, whereAndOpt}
    import cats.syntax.list._
    val idColumnName = DoobieIDColumn[E].id

    val q =
      fr"""DELETE FROM """ ++ tableNameFragment(table) ++
        whereAndOpt(rows.toNel.map(r => in(Fragment.const(columnName(table, idColumnName)), r)))

    q.update
  }

  def getMany[E: DoobieColumnList : DoobieIDColumn : Read, K: Put](table: Table, ids: List[K]): Query0[E] = {
    import Fragments.{in, whereAndOpt}
    import cats.syntax.list._

    val q =
      fr"""SELECT """ ++ tableColumns(table) ++ fr""" from""" ++
        tableNameFragment(table) ++
        whereAndOpt(ids.toNel.map(r => in(Fragment.const(columnName(table, DoobieIDColumn[E].id)), r)))

    q.query[E]
  }

  def whereAnd(fs: Fragment*): Fragment = {
    import Fragments.and
    if (fs.forall(_ == Fragment.empty)) Fragment.empty else fr"WHERE" ++ and(fs: _*)
  }

  def select[E: DoobieColumnList : Read](
    table: Table,
    where: Fragment,
    orderBy: Fragment,
    offset: Fragment,
    limit: Fragment): Query0[E] = {

    val q =
      fr"""SELECT """ ++ tableColumns(table) ++ fr""" from""" ++
        tableNameFragment(table) ++
        whereAnd(where) ++
        orderBy ++
        offset ++
        limit

    q.query[E]
  }

  def deleteBySpecification[E: Read](
    table: Table,
    where: Fragment
  ): Query0[E] = {
    val q =
      fr"""DELETE FROM""" ++
        tableNameFragment(table) ++
        whereAnd(where) ++
        fr"""RETURNING *"""

    q.query[E]
  }
}

abstract class TableRepository[E: DoobieSupport : Read : Write,
                               K: Put : Get]
  extends Repository[ConnectionIO, K, E, Fragment]
    with FindRepository[ConnectionIO, K, E, Fragment]
    with DatabaseRepository {

  import TableRepositorySQL._

  override def create(entity: E)(implicit KE: KeyEntity[E, K]): ConnectionIO[E] = {
    insertOne(table, entity)
      .withUniqueGeneratedKeys[E](DoobieColumnList[E].columns: _*)
  }

  override def create(entities: List[E])(implicit KE: KeyEntity[E, K]): ConnectionIO[List[E]] = {
    insertMany(table)
      .updateManyWithGeneratedKeys[E](DoobieColumnList[E].columns: _*)(entities)
      .compile
      .toList
  }

  override def update(entity: E)(implicit KE: KeyEntity[E, K]): ConnectionIO[Int] = {
    updateOne(table, entity).run
  }

  override def update(entities: List[E])(implicit KE: KeyEntity[E, K]): ConnectionIO[Int] = {
    val entitiesWithIDs = entities.map(e => (e, KE.key(e)))
    updateMany[E, K](table).updateMany(entitiesWithIDs)
  }

  override def get(entityId: K): ConnectionIO[Option[E]] =
    getMany(table, List(entityId)).option

  override def get(entityIds: List[K]): ConnectionIO[List[E]] =
    getMany(table, entityIds).to[List]

  override def deleteById(entityId: K): ConnectionIO[Int] =
    deleteManyIDs(table, List(entityId)).run

  override def deleteByIds(entityIds: List[K]): ConnectionIO[Int] =
    deleteManyIDs(table, entityIds).run

  override def deleteEntity(entity: E)(implicit KE: KeyEntity[E, K]): ConnectionIO[Int] =
    deleteManyIDs[E, K](table, List(KE.key(entity))).run

  override def deleteEntities(entities: List[E])(implicit KE: KeyEntity[E, K]): ConnectionIO[Int] =
    deleteManyIDs(table, entities.map(entity => KE.key(entity))).run

  override def delete(specification: Fragment)(implicit KE: KeyEntity[E, K]): ConnectionIO[List[E]] =
    deleteBySpecification[E](
      table = table,
      where = specification
    ).to[List]

  override def find(specification: Fragment, orderBy: Option[String], limit: Option[Int], offset: Option[Int]): ConnectionIO[List[E]] =
    select(
      table = table,
      where = specification,
      orderBy = if (orderBy.forall(_.isEmpty)) Fragment.empty else Fragment.const(orderBy.get),
      offset = if (offset.isEmpty) Fragment.empty else Fragment.const(s"OFFSET ${offset.get}"),
      limit = if (limit.isEmpty) Fragment.empty else Fragment.const(s"LIMIT ${limit.get}")
    ).to[List]

  override def listAll: doobie.ConnectionIO[List[E]] =
    find(Fragment.empty, None, None, None)
}
