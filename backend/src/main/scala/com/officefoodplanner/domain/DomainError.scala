package com.officefoodplanner.domain

import scala.util.control.NoStackTrace

abstract class DomainError(detailMessage: String = "") extends NoStackTrace with Product with Serializable
