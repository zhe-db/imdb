package edu.duke.imdb.models.entity

case class PaginatedResult[T](
    totalCount: Int,
    entities: List[T],
    hasNextPage: Boolean
)
