package org.codecannery.lunchplanner.infrastructure.repository.inmemory

import cats.Applicative
import cats.implicits._

import scala.collection.concurrent.TrieMap

abstract class InMemoryRepository[F[_]: Applicative, E, K] {

  protected val cache = new TrieMap[K, E]

  def entityKey(e: E): K

  def create(e: E): F[E] = {
    cache.put(entityKey(e),  e)
    e.pure[F]
  }

  def update(e: E): F[Int] = cache.get(entityKey(e)).map { _ =>
    cache.update(entityKey(e), e)
    1
  }.getOrElse(0).pure[F]

  def get(id: K): F[Option[E]] = cache.get(id).pure[F]

  def delete(id: K): F[Int] = cache.remove(id).map(_ => 1).getOrElse(0).pure[F]

}
