package org.codecannery.lunchplanner.infrastructure.repository

import doobie.ConnectionIO
import doobie.util.fragment.Fragment
import doobie.syntax.string._
import doobie.util.query.Query0

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

  def deleteById(entityId: K): ConnectionIO[Int]

  def deleteByIds(entityIds: List[K]): ConnectionIO[Int]

  def deleteEntity(entity: E): ConnectionIO[Int]

  def deleteEntities(entities: List[E]): ConnectionIO[Int]

  protected def delete(specification: Specification): Query0[E]

  def list: ConnectionIO[List[E]] =
    find(specification = Specification.empty).to[List]

  def list(pageSize: Int, offset: Int): ConnectionIO[List[E]] =
    find(
      specification = Specification.empty,
      limit = Limit(fr"LIMIT $pageSize"),
      offset = Offset(fr"OFFSET $offset")
    ).to[List]

  protected def find(specification: Specification): Query0[E]
  protected def find(specification: Specification, orderBy: OrderBy): Query0[E]
  protected def find(specification: Specification, limit: Limit): Query0[E]
  protected def find(specification: Specification, limit: Limit, offset: Offset): Query0[E]
  protected def find(
      specification: Specification,
      orderByFragment: OrderBy,
      limit: Limit,
      offset: Offset): Query0[E]
}
