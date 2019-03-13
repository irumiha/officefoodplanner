package org.codecannery.lunchplanner.infrastructure.repository

import java.util.UUID

case class SchemaName(v: String) extends AnyVal
case class TableName(v: String)  extends AnyVal
case class Table(schemaName: SchemaName = SchemaName("public"), tableName: TableName)

trait KeyedEntity[E, K] {
  def key(e: E): K
}

trait UuidKeyEntity[E] extends KeyedEntity[E, UUID] {
  override def key(e: E): UUID
}
object UuidKeyEntity {
  def apply[E](implicit instance: UuidKeyEntity[E]): UuidKeyEntity[E] = instance
}

trait LongKeyEntity[E] extends KeyedEntity[E, Long] {
  override def key(e: E): Long
}
object LongKeyEntity {
  def apply[E](implicit instance: LongKeyEntity[E]): LongKeyEntity[E] = instance
}

trait TableEntity[E] {
  def table(e: E): Table
}
object TableEntity {
  def apply[E](implicit instance: TableEntity[E]): TableEntity[E] = instance
}

