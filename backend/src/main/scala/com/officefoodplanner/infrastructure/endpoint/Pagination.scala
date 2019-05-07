package com.officefoodplanner.infrastructure.endpoint

import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object Pagination {

  /* Necessary for decoding query parameters */

  /* Parses out the optional offset and page size params */
  object PageSizeQ extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
  object OffsetQ extends OptionalQueryParamDecoderMatcher[Int]("offset")
}
