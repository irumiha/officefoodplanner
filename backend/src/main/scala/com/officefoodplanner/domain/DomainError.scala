package com.officefoodplanner.domain

import scala.util.control.NoStackTrace

abstract class DomainError extends NoStackTrace with Product with Serializable
