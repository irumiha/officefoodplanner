package org.codecannery.lunchplanner.domain

trait Entity[K] {
  val key: K
}

trait Repository[F[_], K, E <: Entity[K]] {
  def create(entity: E): F[E]

  def create(entities: Seq[E]): F[Seq[E]]

  def update(entity: E): F[Option[E]]

  def update(entityies: Seq[E]): F[Seq[E]]

  def get(entityId: K): F[Option[E]]

  def get(entityIds: Seq[K]): F[Seq[E]]

  def delete(entityId: K): F[Option[E]]

  def delete(entityIds: Seq[K]): F[Seq[E]]

  def deleteEntity(entity: E): F[Option[E]]

  def deleteEntities(entities: Seq[E]): F[Seq[E]]

  def list(pageSize: Int, offset: Int): F[Seq[E]] =
    find(specification = "", orderBy = None, pageSize = Some(pageSize), offset = Some(offset))

  protected def find(specification: String,
                     orderBy: Option[String],
                     pageSize: Option[Int],
                     offset: Option[Int]): F[Seq[E]]
}
