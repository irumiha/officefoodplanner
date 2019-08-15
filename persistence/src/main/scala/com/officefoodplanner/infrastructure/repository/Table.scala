package com.officefoodplanner.infrastructure.repository

case class SchemaName(v: String) extends AnyVal
case class TableName(v: String) extends AnyVal
case class Table(schemaName: SchemaName = SchemaName("public"), tableName: TableName) {
  override def toString: String = s""""${schemaName.v}"."${tableName.v}""""
}
