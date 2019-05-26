package com.officefoodplanner.infrastructure.repository.inmemory

import cats._
import cats.implicits._
import com.officefoodplanner.infrastructure.repository.{KeyEntity, Repository}

import scala.collection.concurrent.TrieMap

import scala.language.higherKinds

class InMemoryRepository[F[_]: Applicative, E, K]
  extends Repository[F, K, E, E => Boolean]
{

  private val cache = new TrieMap[K, E]

  override def get(entityId: K): F[Option[E]] =
    cache.get(entityId).pure[F]

  override def get(entityIds: List[K]): F[List[E]] =
    entityIds
      .map(cache.get)
      .collect {
        case Some(e) => e
      }
      .pure[F]

  override def listAll: F[List[E]] =
    cache.values.toList.pure[F]

  override def create(entity: E)(implicit KE: KeyEntity[E, K]): F[E] = {
    cache += (KE.key(entity) -> entity)
    entity.pure[F]
  }

  override def create(entities: List[E])(implicit KE: KeyEntity[E, K]): F[List[E]] =
    entities
      .map { e =>
        cache += (KE.key(e) -> e)
        e
      }
      .pure[F]

  override def update(entity: E)(implicit KE: KeyEntity[E, K]): F[Int] = {
    val key = KE.key(entity)

    if (cache.contains(key)) {
      cache.update(key, entity)
      1.pure[F]
    } else {
      0.pure[F]
    }
  }

  override def deleteById(entityId: K): F[Int] =
    cache.remove(entityId).toList.length.pure[F]

  override def deleteByIds(entityIds: List[K]): F[Int] =
    entityIds.map(deleteById).sequence.map(_.sum)

  override def deleteEntity(entity: E)(implicit KE: KeyEntity[E, K]): F[Int] = {
    val key = KE.key(entity)
    deleteById(key)
  }

  override def deleteEntities(entities: List[E])(implicit KE: KeyEntity[E, K]): F[Int] =
    entities.map(deleteEntity).sequence.map(_.sum)

  override def delete(specification: E => Boolean)(implicit KE: KeyEntity[E, K]): F[List[E]] = {
    val toDelete = cache.filter { case (_, v) => specification(v) }.values.toList
    deleteEntities(toDelete)
    toDelete.pure[F]
  }


}
