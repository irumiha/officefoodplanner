package org.codecannery.lunchplanner.domain.users.model

import java.util.UUID

import org.codecannery.lunchplanner.domain.Entity

case class User(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    override val key: UUID = UUID.randomUUID()
) extends Entity[UUID]
