package org.codecannery.lunchplanner.domain.users.view

case class UserListView(
    userName: String,
    firstName: String,
    lastName: String,
    email: String,
    hash: String,
    phone: String,
    id: Option[Long] = None
)
