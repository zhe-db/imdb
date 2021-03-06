package edu.duke.imdb.models.entity

/** Entity class storing rows of table Moviedetail
  * @param id
  *   Database column id SqlType(int4), PrimaryKey
  * @param adult
  *   Database column adult SqlType(bool)
  * @param backdropPath
  *   Database column backdrop_path SqlType(text), Default(None)
  * @param budget
  *   Database column budget SqlType(int4), Default(None)
  * @param imdbId
  *   Database column imdb_id SqlType(text)
  * @param title
  *   Database column title SqlType(text)
  * @param overview
  *   Database column overview SqlType(text), Default(None)
  * @param popularity
  *   Database column popularity SqlType(float8), Default(None)
  * @param posterPath
  *   Database column poster_path SqlType(text), Default(None)
  * @param releaseDate
  *   Database column release_date SqlType(date), Default(None)
  * @param runtime
  *   Database column runtime SqlType(int4), Default(None)
  * @param revenue
  *   Database column revenue SqlType(int4), Default(None)
  * @param voteAverage
  *   Database column vote_average SqlType(float8), Default(None)
  */
case class MovieDetailRow(
    id: Int,
    adult: Boolean,
    backdropPath: Option[String] = None,
    budget: Option[Int] = None,
    imdbId: String,
    title: String,
    overview: Option[String] = None,
    popularity: Option[Double] = None,
    posterPath: Option[String] = None,
    releaseDate: Option[java.sql.Date] = None,
    runtime: Option[Int] = None,
    revenue: Option[Int] = None,
    voteAverage: Option[Double] = None,
    homepage: Option[String] = None,
    voteCount: Option[Int] = None,
    tagline: Option[String] = None
)

case class MovieDetailRows(movies: Seq[MovieDetailRow])

/** Entity class storing rows of table Moviegenre
  * @param movieId
  *   Database column movie_id SqlType(int4)
  * @param genreId
  *   Database column genre_id SqlType(int4)
  */

case class CompleteMovie(
    var movie: MovieDetailRow,
    var genres: Genres,
    var crews: MovieCrews
)

case class MovieListItem(
    id: Int,
    title: String,
    posterPath: Option[String],
    voteAverage: Option[Double],
    voteCount: Option[Int]
)

case class MovieCrews(crews: Seq[MovieCrew])

case class MovieGenre(movieId: Int, genreId: Int)

case class Genre(id: Int, name: String)

case class Genres(genres: Seq[Genre])

case class GenreCount(genre: Genre, count: Int)

case class GenreCounts(genres: Seq[GenreCount])

case class MovieCrew(
    movieId: Int,
    crewId: Int,
    types: Option[String] = None,
    castId: Int,
    character: Option[String] = None,
    job: Option[String] = None,
    department: Option[String] = None,
    creditId: Option[String] = None,
    ordering: Option[Int] = None
)
