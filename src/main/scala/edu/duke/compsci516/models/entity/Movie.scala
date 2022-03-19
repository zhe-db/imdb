package edu.duke.compsci516.models.entity

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
    voteAverage: Option[Double] = None
)

case class Movie(
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
    voteAverage: Option[Double] = None
)

/** Entity class storing rows of table Moviegenre
  * @param movieId
  *   Database column movie_id SqlType(int4)
  * @param genreId
  *   Database column genre_id SqlType(int4)
  */
case class MovieGenre(movieId: Int, genreId: Int)

case class Genre(id: Int, name: String)

case class Genres(genres: Seq[Genre])
