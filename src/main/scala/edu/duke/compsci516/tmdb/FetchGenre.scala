package edu.duke.compsci516.tmdb

import java.nio.file.Paths
import scala.concurrent.duration.DurationInt
import edu.duke.compsci516.tmdb.api.Protocol._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.concurrent._
import ExecutionContext.Implicits.global

import edu.duke.compsci516.components.ConfigComponent
import edu.duke.compsci516.tmdb.client.TmdbClient

import edu.duke.compsci516.models.entity
import edu.duke.compsci516.models.components.GenreRepository
import edu.duke.compsci516.components.DatabaseComponent

object FetchGenre extends ConfigComponent with DatabaseComponent {
  val apiKey = config.getString("tmdb.apiKey")
  val genreRepo = new GenreRepository(this.db)
  val tmdbClient = apiKey match {
    case key: String => fetchGenre(TmdbClient(key, "en-US"))
    case _ =>
      System.err.println(
        "API Key need to be available as an environment variable named apiKey"
      )
      System.exit(1)
  }
  private def fetchGenre(tmdbClient: TmdbClient) = {

    implicit val timeout = 10.seconds

    val token = Try(Await.result(tmdbClient.getToken, timeout).request_token)
    token match {
      case Success(_) =>
        tmdbClient.log.info(s"OK got valid token : ${token.get}")
      case Failure(e) =>
        tmdbClient.log.info(e.getMessage)
        tmdbClient.shutdown()
        System.exit(1)
    }

    val genres = Await.result(tmdbClient.getGenres(), timeout)
    for (genre <- genres.genres) {
      val dbGenre = new entity.Genre(genre.id, genre.name)
      genreRepo.add(dbGenre).onComplete {
        case Success(res) =>
          tmdbClient.log.info(
            s"OK got genre : ${genre.id}: ${genre.name}"
          )
        case Failure(f) =>
          tmdbClient.log.error(
            s"f"
          )

      }
    }

    tmdbClient.shutdown()
  }
}
