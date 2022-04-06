package edu.duke.imdb.models.delta

import edu.duke.imdb.models.entity.MovieDetailRow

case class DeltaMovieDetailRow(
    id: String,
    adult: Boolean,
    backdropPath: Option[String] = None,
    budget: Option[Int] = None,
    imdbId: String,
    title: String,
    overview: Option[String] = None,
    popularity: Option[Double] = None,
    posterPath: Option[String] = None,
    runtime: Option[Int] = None,
    revenue: Option[Int] = None,
    voteAverage: Option[Double] = None,
    homepage: Option[String] = None,
    voteCount: Option[Int] = None,
    tagline: Option[String] = None
) {
  def this(movie: MovieDetailRow) = this(
    movie.id.toString(),
    movie.adult,
    movie.backdropPath,
    movie.budget,
    movie.imdbId,
    movie.title,
    movie.overview,
    movie.popularity,
    movie.posterPath,
    movie.runtime,
    movie.revenue,
    movie.voteAverage,
    movie.homepage,
    movie.voteCount,
    movie.tagline
  )
}
