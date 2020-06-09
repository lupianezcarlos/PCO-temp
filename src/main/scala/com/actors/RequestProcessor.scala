package com.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import akka.pattern.pipe
import akka.util.ByteString

import scala.concurrent.Future

class RequestProcessor extends Actor with ActorLogging {
  import context.dispatcher
  import RequestProcessor._

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def doRequest(simpleRequest: SimpleRequest): Future[HttpResponse] = http.singleRequest(HttpRequest(uri = simpleRequest.uri, method = simpleRequest.httpMethod)).pipeTo(self)

  override def receive: Receive = {
    case Get => {

    }
    case request: Request =>

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).mapTo[Response]
    case HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
  }


}




object RequestProcessor {

  trait Response
  trait Request extends Response


  case class SimpleRequest(uri: String, httpMethod: HttpMethod) extends Request
  case object RequestPerformed extends Request



  def props: Props = Props(new RequestProcessor())
  object Get {
    def apply(request: SimpleRequest): Response  = request
  }
  object Post {
    def apply(request: SimpleRequest): Response = request
  }
}
