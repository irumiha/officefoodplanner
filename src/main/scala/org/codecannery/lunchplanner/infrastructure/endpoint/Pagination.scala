package org.codecannery.lunchplanner.infrastructure.endpoint
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object Pagination {

  /* Necessary for decoding query parameters */

  /* Parses out the optional offset and page size params */
  object OptionalPageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
  object OptionalOffsetMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")
}
