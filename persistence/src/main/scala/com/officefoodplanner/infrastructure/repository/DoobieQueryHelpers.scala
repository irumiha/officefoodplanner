package com.officefoodplanner.infrastructure.repository

import doobie.implicits._
import doobie.util.Put
import doobie.util.fragment.Fragment

case class DoobieColumn[E](name: String, value: E => Fragment)
object DoobieColumn {
  def apply[E, C: Put](name: String)(vp: E => C): DoobieColumn[E] =
    DoobieColumn[E](name, e => fr0"${vp(e)}")
}
trait DoobieColumns[E] {
  val columns: List[DoobieColumn[E]]
}
object DoobieColumns {
  def apply[E](implicit instance: DoobieColumns[E]): DoobieColumns[E] = instance
}

trait DoobieIDColumn[E] {
  val id: DoobieColumn[E]
}
object DoobieIDColumn {
  def apply[E](implicit instance: DoobieIDColumn[E]): DoobieIDColumn[E] = instance
}

trait DoobieSupport[E] extends DoobieColumns[E] with DoobieIDColumn[E]
object DoobieSupport {
  def apply[E](implicit instance: DoobieSupport[E]): DoobieSupport[E] = instance
}
