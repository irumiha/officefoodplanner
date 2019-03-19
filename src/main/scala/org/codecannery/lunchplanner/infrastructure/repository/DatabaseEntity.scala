package org.codecannery.lunchplanner.infrastructure.repository

import java.util.UUID

trait KeyedEntity[E, K] {
  def key(e: E): K
}
object KeyedEntity {
  def apply[E, K](implicit instance: KeyedEntity[E, K]): KeyedEntity[E, K] = instance
}

trait UuidKeyEntity[E] extends KeyedEntity[E, UUID] {
  override def key(e: E): UUID
}
object UuidKeyEntity {
  def apply[E](implicit instance: UuidKeyEntity[E]): UuidKeyEntity[E] = instance
}

trait LongKeyEntity[E] extends KeyedEntity[E, Long] {
  override def key(e: E): Long
}
object LongKeyEntity {
  def apply[E](implicit instance: LongKeyEntity[E]): LongKeyEntity[E] = instance
}
