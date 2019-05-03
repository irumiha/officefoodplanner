package org.codecannery.lunchplanner.infrastructure.repository

import java.util.UUID

trait KeyEntity[E, K] {
  def key(e: E): K
}
object KeyEntity {
  def apply[E, K](implicit instance: KeyEntity[E, K]): KeyEntity[E, K] = instance
}

trait UuidKeyEntity[E] {
  def key(e: E): UUID
}
object UuidKeyEntity {
  def apply[E](implicit instance: UuidKeyEntity[E]): UuidKeyEntity[E] = instance
}

trait LongKeyEntity[E] {
  def key(e: E): Long
}
object LongKeyEntity {
  def apply[E](implicit instance: LongKeyEntity[E]): LongKeyEntity[E] = instance
}
