package com.officefoodplanner.domain.auth.model

import java.util.UUID

import com.officefoodplanner.infrastructure.repository.KeyEntity

case class Group(
    id: UUID = UUID.randomUUID(),
    name: String
)

object Group {
  implicit val keyedGroup: KeyEntity[Group, UUID] = e => e.id
}
