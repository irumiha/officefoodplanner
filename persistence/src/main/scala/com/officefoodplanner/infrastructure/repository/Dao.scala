package com.officefoodplanner.infrastructure.repository

trait WriteDao[F[_], E] {
  type Key

  def create(entity: E): F[E]

  def create(entities: List[E]): F[List[E]]

  def update(entity: E): F[Int]

  def update(entities: List[E]): F[Int]

  def deleteById(entityId: Key): F[Int]

  def deleteByIds(entityIds: List[Key]): F[Int]

  def deleteEntity(entity: E): F[Int]

  def deleteEntities(entities: List[E]): F[Int]

}

trait ReadDao[F[_], E] {
  type Key

  def get(entityId: Key): F[Option[E]]

  def get(entityIds: List[Key]): F[List[E]]

  def listAll: F[List[E]]
}

trait Dao[F[_], E] extends ReadDao[F, E] with WriteDao[F, E] {
  val idColumn: String
  val columns: List[String]
  lazy val columnsWithoutId: List[String] = columns.filterNot(_ == idColumn)

  lazy val columnsQuoted = s"${columns.mkString("\"", "\",\"", "\"")}"
  lazy val columnsWithoutIdQuoted = s"${columnsWithoutId.mkString("\"", "\",\"", "\"")}"

}
