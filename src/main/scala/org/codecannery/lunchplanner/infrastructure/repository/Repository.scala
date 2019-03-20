package org.codecannery.lunchplanner.infrastructure.repository

import doobie.ConnectionIO
import doobie.util.fragment.Fragment
import doobie.syntax.string._

case class Specification(v: Fragment)
object Specification {
  def empty = Specification(Fragment.empty)
}
case class OrderBy(v: Fragment)
case class Limit(v: Fragment)
case class Offset(v: Fragment)

trait Repository[K, E] {
  def create(entity: E): ConnectionIO[E]

  def create(entities: List[E]): ConnectionIO[List[E]]

  def update(entity: E): ConnectionIO[Int]

  def update(entities: List[E]): ConnectionIO[Int]

  def get(entityId: K): ConnectionIO[Option[E]]

  def get(entityIds: List[K]): ConnectionIO[List[E]]

  def delete(entityId: K): ConnectionIO[Int]

  def delete(entityIds: List[K]): ConnectionIO[Int]

  def deleteEntity(entity: E): ConnectionIO[Int]

  def deleteEntities(entities: List[E]): ConnectionIO[Int]

  def list: ConnectionIO[List[E]] =
    find(specification = Specification.empty)

  def list(pageSize: Int, offset: Int): ConnectionIO[List[E]] =
    find(
      specification = Specification.empty,
      limit = Limit(fr"LIMIT $pageSize"),
      offset = Offset(fr"OFFSET $offset")
    )

  protected def find(specification: Specification): ConnectionIO[List[E]]
  protected def find(specification: Specification, orderBy: OrderBy): ConnectionIO[List[E]]
  protected def find(specification: Specification, limit: Limit): ConnectionIO[List[E]]
  protected def find(specification: Specification, limit: Limit, offset: Offset): ConnectionIO[List[E]]
  protected def find(
      specification: Specification,
      orderByFragment: OrderBy,
      limit: Limit,
      offset: Offset): ConnectionIO[List[E]]
}
