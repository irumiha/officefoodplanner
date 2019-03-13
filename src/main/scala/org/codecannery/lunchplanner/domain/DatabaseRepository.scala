package org.codecannery.lunchplanner.domain

case class SchemaName(v: String) extends AnyVal
case class TableName(v: String)  extends AnyVal
case class Table(schemaName: SchemaName = SchemaName("public"), tableName: TableName)

trait DatabaseRepository {
  def table: Table
}
