package com.officefoodplanner.domain.auth.model

import java.util.UUID

case class Group(
    id: UUID = UUID.randomUUID(),
    name: String
)
