package edu.duke.imdb.tmdb

import java.nio.file.Paths
import scala.io.Source
import scala.concurrent.duration.DurationInt

import edu.duke.imdb.tmdb.api.Protocol._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.text.SimpleDateFormat;

import edu.duke.imdb.tmdb.client.TmdbClient

import edu.duke.imdb.models.entity
import _root_.edu.duke.imdb.models.components._
import _root_.edu.duke.imdb.components._

object FetchMovie extends ConfigComponent with DatabaseComponent {
  val apiKey = config.getString("tmdb.apiKey")
  val movieRepo = new MovieRepository(this.db)
  val genreRepo = new GenreRepository(this.db)
  val crewRepo = new CrewRepository(this.db)
  val movieGenreRepo = new MovieGenreRepository(this.db)
  val movieCrewRepo = new MovieCrewRepository(this.db)
  val timeout = 5.seconds
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
  val defaultDate = dateFormat.parse("1970-01-01")

  val tmdbClient = TmdbClient(apiKey, "en-US")

  def fetchGenres(): Seq[Int] = {

    val genres = Await.result(tmdbClient.getGenres(), timeout)
    for (genre <- genres.genres) {
      val dbGenre = new entity.Genre(genre.id, genre.name)
      genreRepo.add(dbGenre)

    }
    return genres.genres.map(_.id)
  }

  def fetchPopularMovie(startPage: Int, endPage: Int) = {
    for (page <- startPage to endPage) {
      fetchPopularMovieByPage(page)
    }
  }

  def fetchPopularMovieByPage(page: Int) = {
    println("Page: " + page)
    var results =
      try {
        Await.result(tmdbClient.getPopularMoviesByPage(page), timeout * 10)
      } catch {
        case _ =>
          Await.result(tmdbClient.getPopularMoviesByPage(page), timeout * 10)
      }
    results match {
      case r: Results => {
        val movies = r.results
        for (movie <- movies) {
          fetchMovie(movie.id)
        }
      }
    }
  }

  def fetchMovieByYearAndGenre(year: Int, genreId: Int) = {
    var results =
      try {
        Await.result(tmdbClient.getMoviesByYearAndGenre(1, year, genreId), timeout * 10)
      } catch {
        case _ =>
          Await.result(tmdbClient.getMoviesByYearAndGenre(1, year, genreId), timeout * 10)
      }

    results match {
      case r: Results => {
        val maxPages = r.total_pages.min(500)
        for (page <- 1 to maxPages) {
          fetchMovieByYearAndGenreAndPage(page: Int, year: Int, genreId: Int)
        }
      }
    }
  }

  def fetchMovieByYearAndGenreAndPage(page: Int, year: Int, genreId: Int) = {
    println(s"Year: ${year}, Genre: ${genreId}, Page: ${page}")
    var results =
      try {
        Await.result(tmdbClient.getMoviesByYearAndGenre(page, year, genreId), timeout * 10)
      } catch {
        case _ =>
          Await.result(tmdbClient.getMoviesByYearAndGenre(page, year, genreId), timeout * 10)
      }
    results match {
      case r: Results => {
        val movies = r.results
        for (movie <- movies) {
          fetchMovie(movie.id)
        }
      }
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

  def fetchMoviesByYear(startYear: Int, endYear: Int) {
    val genres = fetchGenres()
    for (year <- startYear to endYear) {
      for (genreId <- genres) {
        fetchMovieByYearAndGenre(year, genreId) 
      }
    }
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
            } catch {
              case e: java.util.concurrent.TimeoutException => None
            }
          crewDetail match {
            case crewDetail: CrewDetail => {
              var crewBirthDate = defaultDate
              try {
                crewBirthDate =
                  dateFormat.parse(crewDetail.birthday.getOrElse("1970-01-01"))
              } catch {
                case _ => println(crewDetail.birthday)
              }
              val dbCrew = new entity.Crew(
                crewDetail.id,
                Some(
                  new java.sql.Date(
                    crewBirthDate.getTime()
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
              crewRepo.add(dbCrew).onComplete { _ =>
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
            } catch {
              case _ => None
            }
          crewDetail match {
            case crewDetail: CrewDetail => {
              var crewBirthDate = defaultDate
              try {
                crewBirthDate =
                  dateFormat.parse(crewDetail.birthday.getOrElse("1970-01-01"))
              } catch {
                case _ => println(crewDetail.birthday)
              }
              val dbCrew = new entity.Crew(
                crewDetail.id,
                Some(
                  new java.sql.Date(
                    crewBirthDate.getTime()
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
              crewRepo.add(dbCrew).onComplete { _ =>
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
      } catch {
        case _ => None
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
      } catch {
        case _ => None
      }
    movie match {
      case (movie: Movie) => {
        var movieDate = defaultDate
        try {
          movieDate =
            dateFormat.parse(movie.release_date.getOrElse("1970-01-01"))
        } catch {
          case _ => println(movie.release_date)
        }
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
              movieDate.getTime()
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
        addGenres(movie)
      }
      case _ =>
    }
  }
}
