package  com

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.routes.RestRoutes
import com.actors._
import akka.http.scaladsl.Http
import com.services.HttpClient
import akka.event.{Logging, LoggingAdapter}
import com.api.RestApi

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

object MainService extends App  {

    implicit val system = ActorSystem("fotonesSystem")
    val config = ConfigFactory.load()
    implicit val timeout = Timeout(20 second)
    implicit val materializer = ActorMaterializer()
    implicit  val exc = system.dispatcher
    implicit val log: LoggingAdapter = Logging(system, MainService.getClass)

    val http = HttpClient()


    val routes = new RestApi().routes

  try {
        Http(system).bindAndHandle(routes,"localhost",9099 ).onComplete {
            case Success(value) => println("Server listening on port http://localhost:9099")
            case Failure(exception) => log.error("error message: " + exception.getMessage)
        }
    } catch  {
        case ex:Exception =>
            log.error(ex, "Failed to bind to {}:{}!", "localhost",9099)
    }
//  system.terminate()
}


