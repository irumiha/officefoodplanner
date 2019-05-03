package org.codecannery.lunchplanner.infrastructure.repository

import doobie.util.fragment.Fragment

case class DoobieColumn[E](name: String, value: E => Fragment)

trait DoobieColumns[E] {
  def columns: List[DoobieColumn[E]]
}
object DoobieColumns {
  def apply[E](implicit instance: DoobieColumns[E]): DoobieColumns[E] = instance
}

trait DoobieIDColumn[E] {
  def id: DoobieColumn[E]
}
object DoobieIDColumn {
  def apply[E](implicit instance: DoobieIDColumn[E]): DoobieIDColumn[E] = instance
}

trait DoobieSupport[E] extends DoobieColumns[E] with DoobieIDColumn[E]
object DoobieSupport {
  def apply[E](implicit instance: DoobieSupport[E]): DoobieSupport[E] = instance
}