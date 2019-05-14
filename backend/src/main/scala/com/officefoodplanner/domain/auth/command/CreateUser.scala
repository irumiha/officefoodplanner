package com.officefoodplanner.domain.auth.command

case class CreateUser(
    username: String,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    phone: String
)
