package edu.duke.compsci516.tmdb.api

import java.nio.file.Path

import scala.concurrent.Future
import Protocol.{
  AuthenticateResult,
  Configuration,
  Credits,
  Movie,
  Results,
  Genres,
  CrewDetail
}
import akka.stream.IOResult

trait TmdbApi {
  def getMovie(id: Long): Future[Movie]
  def getGenres(): Future[Genres]
  def getCredits(id: Long): Future[Credits]
  def getConfiguration(): Future[Configuration]
  def getToken(): Future[AuthenticateResult]
  def searchMovie(query: String, page: Int): Future[Results]
  def downloadPoster(movie: Movie, path: Path): Option[Future[IOResult]]
  def getCrewDetail(id: Long): Future[CrewDetail]
  def shutdown(): Unit
}
