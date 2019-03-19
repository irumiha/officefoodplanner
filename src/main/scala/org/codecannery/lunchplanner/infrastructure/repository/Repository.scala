package org.codecannery.lunchplanner.infrastructure.repository

import doobie.free.connection.ConnectionIO
import doobie.util.fragment.Fragment

case class Specification(v: Fragment) extends AnyVal
object Specification {
  def empty = Specification(Fragment.empty)
}
case class OrderBy(v: Fragment) extends AnyVal
case class Limit(v: Fragment) extends AnyVal
case class Offset(v: Fragment) extends AnyVal

trait Repository[F[_], K, E] {
  def create(entity: E): F[ConnectionIO[E]]

  def create(entities: List[E]): F[ConnectionIO[List[E]]]

  def update(entity: E): F[ConnectionIO[Int]]

  def update(entities: List[E]): F[ConnectionIO[Int]]

  def get(entityId: K): F[ConnectionIO[Option[E]]]

  def get(entityIds: List[K]): F[ConnectionIO[List[E]]]

  def delete(entityId: K): F[ConnectionIO[Int]]

  def delete(entityIds: List[K]): F[ConnectionIO[Int]]

  def deleteEntity(entity: E): F[ConnectionIO[Int]]

  def deleteEntities(entities: List[E]): F[ConnectionIO[Int]]

  def list(pageSize: Int, offset: Int): F[ConnectionIO[List[E]]] =
    find(specification = Specification.empty)

  protected def find(specification: Specification): F[ConnectionIO[List[E]]]
  protected def find(specification: Specification, orderBy: OrderBy): F[ConnectionIO[List[E]]]
  protected def find(specification: Specification, limit: Limit): F[ConnectionIO[List[E]]]
  protected def find(specification: Specification, limit: Limit, offset: Offset): F[ConnectionIO[List[E]]]
  protected def find(
      specification: Specification,
      orderByFragment: OrderBy,
      limit: Limit,
      offset: Offset): F[ConnectionIO[List[E]]]
}
