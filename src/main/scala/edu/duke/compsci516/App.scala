package edu.duke.compsci516

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.util.Failure
import scala.util.Success

import edu.duke.compsci516.components._
import edu.duke.compsci516.http.services._
import edu.duke.compsci516.http.routes._

//#main-class
object App extends ConfigComponent with DatabaseComponent {
  val host = config.getString("application.host")
  val port = config.getInt("application.port")

  //#start-http-server
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

  //#start-http-server
  def main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val userRegistryActor =
        context.spawn(UserRegistry(), "UserRegistryActor")
      val genreRegistryActor =
        context.spawn(GenreRegistry(), "GenreRegistryActor")

      context.watch(userRegistryActor)
      context.watch(genreRegistryActor)

      val userRoutes = new UserRoutes(userRegistryActor)(context.system)
      val genreRoutes = new GenreRoutes(genreRegistryActor)(context.system)
      val routes = cors() {concat(userRoutes.userRoutes, genreRoutes.genreRoutes)}
      startHttpServer(routes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
    //#server-bootstrapping
  }
}
//#main-class
