package edu.duke.compsci516.models.entity

case class PaginatedResult[T](
    totalCount: Int,
    entities: List[T],
    hasNextPage: Boolean
)
