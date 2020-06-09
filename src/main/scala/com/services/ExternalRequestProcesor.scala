//package services
//
//import java.io.{PrintWriter, StringWriter}
//
//import akka.actor.Status.Failure
//import akka.actor.{Actor, ActorSystem, Props}
//import akka.pattern.pipe
//import akka.stream.Materializer
//import play.api.Logger
//import services.ExternalRequestProcessor.Protocol.{Error, InternalErrorData, Request, Result}
//
//import scala.concurrent.ExecutionContext
//
//class ExternalRequestProcessor(serviceName: String,
//                               externalRequestServer: RequestServer)
//                              (implicit materializer: Materializer) extends Actor
//  with ServiceCircuitBreaker {
//
//  import context.dispatcher
//
//  val log = Logger("ExternalRequestProcessor")
//  def cbName: String = serviceName
//
//  def breakerOpen: Unit = log.warn(s"${serviceName} - Circuit Breaker open for ${cbSettings.resetTimeout}")
//
//  def receive: Receive = {
//
//    case request: Request => {
//      log.debug("=== REQUEST ===")
//      getResponse(request)
//    }
//
//    case (_, result: Result) =>
//      sender() ! result
//
//    case (_, error: Error) =>
//      sender() ! error
//
//    case failure: Failure =>
//      log.error("Internal failure with ExternalRequestProcessor.")
//      val sw = new StringWriter
//      failure.cause.printStackTrace(new PrintWriter(sw))
//      log.error(sw.toString)
//      sender() ! Error(InternalErrorData(s"Internal failure ${failure.cause}"))
//  }
//
//  private def getResponse(request: Request): Unit = {
//    circuitBreaker.withCircuitBreaker(
//      externalRequestServer.doRequest(request).map(response => request -> response)
//    ).pipeTo(self)(sender())
//  }
//}
//
//object ExternalRequestProcessor {
//  def props(serviceName: String)
//           (implicit materializer: Materializer, system: ActorSystem, executionContext: ExecutionContext): Props =
//    Props(new ExternalRequestProcessor(serviceName, new RequestServerImpl))
//
//  object Protocol {
//
//    sealed trait Request {
//      val data: RequestData
//    }
//
//    trait RequestData
//
//    sealed trait Response {
//      val data: ResponseData
//    }
//
//    trait ResponseData
//
//    case class SimpleRequest(data: RequestData) extends Request
//
//    case class Result(data: ResponseData) extends Response
//
//    case class Error(data: ResponseData) extends Response
//
//    case class InternalErrorData(cause: String) extends ResponseData
//
//  }
//}
