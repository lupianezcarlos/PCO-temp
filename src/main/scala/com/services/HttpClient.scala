package com.services

import akka.actor.{ActorContext, ActorSystem}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.{Http, HttpExt}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import spray.json.JsValue
import play.api.libs.json.{Json}
import play.api.libs.json.Reads._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object HttpClient {
  def apply()(implicit system:ActorSystem, materializer: ActorMaterializer, cx:ExecutionContext) = new HttpClientBase()
}

class HttpClientBase()(implicit  materializer: ActorMaterializer, cx:ExecutionContext, system: ActorSystem) extends HttpClientMaster {
  val self = this;

  val http =  Http()

  //Http get
  final case class get(uri:String)  {
      def getFuture:Future[HttpResponse] = http.singleRequest(request(uri))
      def getResult:ToResponseMarshallable = self.getResult(uri, getFuture)
//      def getJsonResult = self.getJsonResult(uri, getFuture)
  }

  //Http post
  final case class post(uri:String, payload:JsValue)  {
      def getFuture:Future[HttpResponse] = http.singleRequest(request(uri,HttpMethods.POST,
                                           entity=Some(HttpEntity(ContentTypes.`application/json`, payload.toString.getBytes))))
      def getResult:ToResponseMarshallable = self.getResult(uri, getFuture)
  }

  //Http put
  def put:Future[HttpResponse] = ???

  //Http delete
  def delete:Future[HttpResponse] = ???
}


trait HttpClientMaster extends ResponseConverters {

   def request(uri:String,method:HttpMethod = HttpMethods.GET, protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`,
               entity: Option[RequestEntity] = None) = {
                 if(entity.isDefined) { HttpRequest(uri=uri,method=method,protocol=protocol,entity=entity.get)}
                 else HttpRequest(uri=uri,method=method,protocol=protocol)
   }

  def getResult(uri:String, f:Future[HttpResponse])(implicit system:ActorSystem,  materializer:ActorMaterializer, cx:ExecutionContext ) = {

    def handleResponse(uri: String, response: HttpResponse): Future[(Int, String)] = {
        val log = Logging(system,this.getClass)
        response.entity.toStrict(2.seconds).map( c => (response, c.data.utf8String)).map {
          case (resp, content) if resp.status == StatusCodes.OK || resp.status  == StatusCodes.Created =>  (StatusCodes.OK.intValue, content)
          case (resp, content) if resp.status != StatusCodes.OK =>
            log.error(response.httpMessage.toString)
            log.error(s"error found in class: $this.getClass.toString" )
            (resp.status.intValue, "Something was wrong with your request. Contact Support")
        }
     }
     f flatMap(handleResponse(uri, _))
  }

  def getJsonResult(responseF:Future[HttpResponse])(implicit system:ActorSystem,
                                                                 materializer:ActorMaterializer,
                                                                 cx:ExecutionContext) = {
    implicit val writes =

//    val result = new GetJson(responseF)

//    val thisResult = Json.writes[GetJson]
   ""
  }
}

trait ResponseConverters {

  case class  GetJson(responseF:Future[HttpResponse])(implicit system:ActorSystem,  materializer:ActorMaterializer, cx:ExecutionContext) {
    implicit val writes = play.api.libs.json.Writes
    val res = for { response <- responseF } yield {
      response.entity.toStrict(2.seconds).map( c => (response, c.data.utf8String)).map {
        case (resp, content) if resp.status == StatusCodes.OK || resp.status  == StatusCodes.Created => content
        case (resp, content) if resp.status != StatusCodes.OK => "Something when wrong with this request. Please contact support."
      }
    }
    res
  }


  class GetResponseToMarshaller(f:Future[HttpResponse])(implicit system:ActorSystem,  materializer:ActorMaterializer, cx:ExecutionContext) {
    val log = Logging(system,this.getClass)

    private var _status: Int = _
    def status = _status
    private def status_=(status: Int) { _status = status }

    private def toBeFlatted(response: HttpResponse) = {
      response.entity.toStrict(2.seconds).map( c => (response, c.data.utf8String)).map {
        case (resp, content) if resp.status == StatusCodes.OK || resp.status  == StatusCodes.Created =>
          this.status = resp.status.intValue;
          (StatusCodes.OK.intValue, content)
        case (resp, content) if resp.status != StatusCodes.OK =>
          this.status = resp.status.intValue
          log.error(response.httpMessage.toString)
          log.error(s"error found in class: $this.getClass.toString" )
          (resp.status.intValue, "Something was wrong with your request. Contact Support")
      }
    }
    f flatMap(toBeFlatted(_))
  }
}