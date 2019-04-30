package org.codecannery.lunchplanner.infrastructure.repository

import doobie.util.fragment.Fragment

trait DoobieColumnList[E] {
  def columns: List[String]
}
object DoobieColumnList {
  def apply[E](implicit instance: DoobieColumnList[E]): DoobieColumnList[E] = instance
}

trait DoobieIDColumn[E] {
  def id: String
}
object DoobieIDColumn {
  def apply[E](implicit instance: DoobieIDColumn[E]): DoobieIDColumn[E] = instance
}

trait DoobieColumnValues[E] {
  def values(e: E): List[Fragment]
}
object DoobieColumnValues {
  def apply[E](implicit instance: DoobieColumnValues[E]): DoobieColumnValues[E] = instance
}

trait DoobieSupport[E] extends DoobieColumnList[E] with DoobieIDColumn[E] with DoobieColumnValues[E]
object DoobieSupport {
  def apply[E](implicit instance: DoobieSupport[E]): DoobieSupport[E] = instance
}