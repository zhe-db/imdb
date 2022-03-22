package edu.duke.compsci516.tmdb.client

class TmdbException(
    message: String = null,
    cause: Throwable = null,
    val code: Int
) extends RuntimeException(message, cause)

class InvalidApiKeyException(
    message: String = null,
    cause: Throwable = null,
    code: Int
) extends TmdbException(message, cause, code)

object TmdbException {
  def apply(
      message: String = null,
      cause: Throwable = null,
      code: Int = -1
  ): TmdbException =
    new TmdbException(message, cause, code)
}
