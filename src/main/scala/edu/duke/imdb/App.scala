package edu.duke.imdb

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.model.HttpOriginMatcher
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

import scala.util.Failure
import scala.util.Success

import _root_.edu.duke.imdb.components._
import _root_.edu.duke.imdb.core.services._
import _root_.edu.duke.imdb.core.routes._
import _root_.edu.duke.imdb.core.utils.CORSHandler
import akka.http.javadsl.model.headers

//#main-class
object App extends ConfigComponent with DatabaseComponent with CORSHandler {
  val host = config.getString("application.host")
  val port = config.getInt("application.port")

  println(host + ":" + port)
  // #start-http-server
  private def startHttpServer(
      routes: Route
  )(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt(this.host, this.port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  // #start-http-server
  def main(args: Array[String]): Unit = {
    // #server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val userRegistryActor =
        context.spawn(UserRegistry(), "UserRegistryActor")
      val genreRegistryActor =
        context.spawn(GenreRegistry(), "GenreRegistryActor")
      val movieRegistryActor =
        context.spawn(MovieRegistry(), "MovieRegistryActor")
      val userReviewActor =
        context.spawn(UserReviewActor(), "UserReviewActor")

      context.watch(userRegistryActor)
      context.watch(genreRegistryActor)
      context.watch(movieRegistryActor)

      val genreRoutes = new GenreRoutes(genreRegistryActor)(context.system)
      val movieRoutes = new MovieRoutes(movieRegistryActor)(context.system)
      val userReviewRoutes =
        new UserReviewRoutes(userReviewActor)(context.system)

      val userRoutes =
        new UserRoutes(userRegistryActor, userReviewRoutes.reviewRoutes)(
          context.system
        )

      val corsSettings = CorsSettings.defaultSettings
        .withAllowedOrigins(
          HttpOriginMatcher.*
        )
        .withAllowCredentials(true)
      val routes = corsHandler {
        concat(
          userRoutes.userRoutes,
          genreRoutes.genreRoutes,
          movieRoutes.movieRoutes
        )
      }
      startHttpServer(routes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
    // #server-bootstrapping
  }
}
//#main-class
