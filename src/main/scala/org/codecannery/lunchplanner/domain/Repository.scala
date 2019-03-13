package org.codecannery.lunchplanner.domain

trait Repository[F[_], K, E] {
  def create(entity: E): F[Int]

  def create(entities: Seq[E]): F[Int]

  def update(entity: E): F[Int]

  def update(entities: Seq[E]): F[Int]

  def get(entityId: K): F[Option[E]]

  def get(entityIds: Seq[K]): F[Seq[E]]

  def delete(entityId: K): F[Int]

  def delete(entityIds: Seq[K]): F[Int]

  def deleteEntity(entity: E): F[Int]

  def deleteEntities(entities: Seq[E]): F[Int]

  def list(pageSize: Int, offset: Int): F[Seq[E]] =
    find(specification = "", orderBy = None, pageSize = Some(pageSize), offset = Some(offset))

  protected def find(specification: String,
                     orderBy: Option[String],
                     pageSize: Option[Int],
                     offset: Option[Int]): F[Seq[E]]
}
