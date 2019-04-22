package org.codecannery.lunchplanner.infrastructure.repository.inmemory

import java.util.UUID

import cats._
import cats.implicits._
import org.codecannery.lunchplanner.infrastructure.repository.{Repository, UuidKeyEntity}

import scala.collection.concurrent.TrieMap

class InMemoryUUIDKeyRepository[F[_]: Applicative, E: UuidKeyEntity]
  extends Repository[F, UUID, E, E => Boolean]
{

  private val cache = new TrieMap[UUID, E]

  override def get(entityId: UUID): F[Option[E]] =
    cache.get(entityId).pure[F]

  override def get(entityIds: List[UUID]): F[List[E]] =
    entityIds
      .map(cache.get)
      .collect {
        case Some(e) => e
      }
      .pure[F]

  override def listAll: F[List[E]] =
    cache.values.toList.pure[F]

  override def create(entity: E): F[E] = {
    cache += (UuidKeyEntity[E].key(entity) -> entity)
    entity.pure[F]
  }

  override def create(entities: List[E]): F[List[E]] =
    entities
      .map { e =>
        cache += (UuidKeyEntity[E].key(e) -> e)
        e
      }
      .pure[F]

  override def update(entity: E): F[Int] = {
    val key = UuidKeyEntity[E].key(entity)

    if (cache.contains(key)) {
      cache.update(UuidKeyEntity[E].key(entity), entity)
      1.pure[F]
    } else {
      0.pure[F]
    }
  }

  override def update(entities: List[E]): F[Int] =
    entities
      .map { entity =>
        update(entity)
      }
      .sequence
      .map(_.sum)

  override def deleteById(entityId: UUID): F[Int] =
    cache.remove(entityId).toList.length.pure[F]

  override def deleteByIds(entityIds: List[UUID]): F[Int] =
    entityIds.map(deleteById).sequence.map(_.sum)

  override def deleteEntity(entity: E): F[Int] = {
    val key = UuidKeyEntity[E].key(entity)
    deleteById(key)
  }

  override def deleteEntities(entities: List[E]): F[Int] =
    entities.map(deleteEntity).sequence.map(_.sum)

  override def delete(specification: E => Boolean): F[List[E]] = {
    val toDelete = cache.filter { case (_, v) => specification(v) }.values.toList
    deleteEntities(toDelete)
    toDelete.pure[F]
  }


}
