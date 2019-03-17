package org.codecannery.lunchplanner.infrastructure.repository

import doobie.util.fragment.Fragment

case class Specification(v: Fragment) extends AnyVal
object Specification {
  def empty = Specification(Fragment.empty)
}
case class OrderBy(v: Fragment) extends AnyVal
case class Limit(v: Fragment) extends AnyVal
case class Offset(v: Fragment) extends AnyVal

trait Repository[F[_], K, E] {
  def create(entity: E): F[E]

  def create(entities: List[E]): F[List[E]]

  def update(entity: E): F[Int]

  def update(entities: List[E]): F[Int]

  def get(entityId: K): F[Option[E]]

  def get(entityIds: List[K]): F[List[E]]

  def delete(entityId: K): F[Int]

  def delete(entityIds: List[K]): F[Int]

  def deleteEntity(entity: E): F[Int]

  def deleteEntities(entities: List[E]): F[Int]

  def list(pageSize: Int, offset: Int): F[List[E]] =
    find(specification = Specification.empty)

  protected def find(specification: Specification): F[List[E]]
  protected def find(specification: Specification, orderBy: OrderBy): F[List[E]]
  protected def find(specification: Specification, limit: Limit): F[List[E]]
  protected def find(specification: Specification, limit: Limit, offset: Offset): F[List[E]]
  protected def find(
      specification: Specification,
      orderByFragment: OrderBy,
      limit: Limit,
      offset: Offset): F[List[E]]
}
