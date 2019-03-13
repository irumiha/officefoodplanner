package org.codecannery.lunchplanner.domain

import java.util.UUID

trait Repository[F[_], K, E] {
  def create(entity: E): F[E]

  def create(entities: Seq[E]): F[Seq[E]]

  def update(entity: E): F[Option[E]]

  def update(entities: Seq[E]): F[Seq[E]]

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
