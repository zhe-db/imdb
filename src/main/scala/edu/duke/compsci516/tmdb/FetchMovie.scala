package edu.duke.compsci516.tmdb

import java.nio.file.Paths
import scala.io.Source
import scala.concurrent.duration.DurationInt

import edu.duke.compsci516.tmdb.api.Protocol._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.text.SimpleDateFormat;

import edu.duke.compsci516.tmdb.client.TmdbClient

import edu.duke.compsci516.models.entity
import _root_.edu.duke.compsci516.models.components._
import _root_.edu.duke.compsci516.components._

object FetchMovie extends ConfigComponent with DatabaseComponent {
  val apiKey = config.getString("tmdb.apiKey")
  val movieRepo = new MovieRepository(this.db)
  val genreRepo = new GenreRepository(this.db)
  val crewRepo = new CrewRepository(this.db)
  val movieGenreRepo = new MovieGenreRepository(this.db)
  val movieCrewRepo = new MovieCrewRepository(this.db)
  val timeout = 5.seconds
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  val tmdbClient = TmdbClient(apiKey, "en-US")

  def fetchGenres() = {

    val genres = Await.result(tmdbClient.getGenres(), timeout)
    for (genre <- genres.genres) {
      val dbGenre = new entity.Genre(genre.id, genre.name)
      genreRepo.add(dbGenre)

    }
  }

  def fetchMoviesFromLinks() {
    val link_file = config.getString("recommendation.linkFile")
    val bufferedSource = Source.fromFile(link_file)
    for (line <- bufferedSource.getLines.drop(1)) {
      val cols = line.split(",").map(_.trim)
      // do whatever you want with the columns here
      if (cols.length == 3) {
        val movieTMDBid = cols(2).toInt
        fetchMovie(movieTMDBid)
      }
    }
    bufferedSource.close
  }

  def addDirectors(movie: Movie, credits: Credits) = {
    val directors = credits.crew.find(crew => crew.job == "Director")
    for (director <- directors) {
      crewRepo.get(director.id).onComplete {
        case Success(Some(res: entity.Crew)) =>
          val movieCrew = new entity.MovieCrew(
            movie.id,
            director.id,
            Some("Director"),
            0,
            None,
            Some(director.job),
            director.departement,
            Some(director.credit_id),
            None
          )
          movieCrewRepo.add(movieCrew)

        case _ =>
          val crewDetail =
            try {
              Await.result(tmdbClient.getCrewDetail(director.id), timeout)
            }
          crewDetail match {
            case crewDetail: CrewDetail => {
              val dbCrew = new entity.Crew(
                crewDetail.id,
                Some(
                  new java.sql.Date(
                    dateFormat
                      .parse(crewDetail.birthday.getOrElse("1970-01-01"))
                      .getTime()
                  )
                ),
                crewDetail.known_for_department,
                crewDetail.name,
                Some(crewDetail.gender),
                crewDetail.biography,
                crewDetail.place_of_birth,
                crewDetail.profile_path,
                crewDetail.homepage,
                crewDetail.imdb_id
              )
              crewRepo.add(dbCrew)
              val movieCrew = new entity.MovieCrew(
                movie.id,
                director.id,
                Some("Director"),
                0,
                None,
                Some(director.job),
                director.departement,
                Some(director.credit_id),
                None
              )
              movieCrewRepo.add(movieCrew)
            }
            case _ =>
          }
      }
    }

  }

  def addCasts(movie: Movie, credits: Credits) = {
    val casts = credits.cast
    for (cast <- casts) {
      crewRepo.get(cast.id).onComplete {
        case Success(Some(res: entity.Crew)) =>
          val movieCrew = new entity.MovieCrew(
            movie.id,
            cast.id,
            Some("Cast"),
            cast.cast_id,
            cast.character,
            Some("Cast"),
            Some("Cast"),
            Some(cast.credit_id),
            Some(cast.order)
          )
          movieCrewRepo.add(movieCrew)

        case _ =>
          val crewDetail =
            try {
              Await.result(tmdbClient.getCrewDetail(cast.id), timeout)
            }
          crewDetail match {
            case crewDetail: CrewDetail => {
              val dbCrew = new entity.Crew(
                crewDetail.id,
                Some(
                  new java.sql.Date(
                    dateFormat
                      .parse(crewDetail.birthday.getOrElse("1970-01-01"))
                      .getTime()
                  )
                ),
                crewDetail.known_for_department,
                crewDetail.name,
                Some(crewDetail.gender),
                crewDetail.biography,
                crewDetail.place_of_birth,
                crewDetail.profile_path,
                crewDetail.homepage,
                crewDetail.imdb_id
              )
              crewRepo.add(dbCrew)
              val movieCrew = new entity.MovieCrew(
                movie.id,
                cast.id,
                Some("Cast"),
                cast.cast_id,
                cast.character,
                Some("Cast"),
                Some("Cast"),
                Some(cast.credit_id),
                Some(cast.order)
              )
              movieCrewRepo.add(movieCrew)
            }
            case _ =>
          }
      }
    }
  }

  def addCrews(movie: Movie) = {
    val credits =
      try {
        Await.result(tmdbClient.getCredits(movie.id), timeout)
      }
    credits match {
      case credits: Credits => {
        addDirectors(movie, credits)
        addCasts(movie, credits)
      }
      case _ =>
    }
  }

  def addGenres(movie: Movie) = {
    val genres = movie.genres
    for (genre <- genres) {
      movieGenreRepo.add(entity.MovieGenre(movie.id, genre.id))
    }
  }

  def fetchMovie(movieId: Int) = {
    val movie =
      try {
        Await.result(tmdbClient.getMovie(movieId), timeout)
      }
    movie match {
      case (movie: Movie) => {
        val dbMovie = entity.MovieDetailRow(
          movie.id,
          movie.adult,
          movie.backdrop_path,
          Some(movie.budget),
          movie.imdb_id.getOrElse(""),
          movie.title.getOrElse(""),
          movie.overview,
          Some(movie.popularity),
          movie.poster_path,
          Some(
            new java.sql.Date(
              dateFormat
                .parse(movie.release_date.getOrElse("1970-01-01"))
                .getTime()
            )
          ),
          movie.runtime,
          Some(movie.revenue),
          movie.vote_average,
          movie.homepage,
          Some(movie.vote_count),
          movie.tagline
        )
        movieRepo.add(dbMovie)
        addCrews(movie)
      }
      case _ =>
    }
  }
}
