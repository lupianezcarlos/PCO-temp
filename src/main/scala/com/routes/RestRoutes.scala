package com.routes

import com.utils.HttpUtils
import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route, RouteResult}
import akka.stream.ActorMaterializer
import com.api.JsonSupport
import spray.json.{DefaultJsonProtocol, JsonParser, ParserInput}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContext, Future}
import akka.event.LoggingAdapter
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsValue, Json}
import java.nio.charset.StandardCharsets
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit

import com.db.Connection
import akka.http.javadsl.Http
import akka.http.scaladsl.marshalling.{Marshal, ToEntityMarshaller}
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.directives.Credentials
import com.utils.FileUtil

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.util.Timeout
import com.actors.ImageProcessor.ImageProcessorResponse
import com.actors.supervisors.ImageTaskDistributor.{ImageTaskDistributorResponse, ProcessImages}
import com.actors.supervisors.strategy.CrashRequest
import com.actors.supervisors.ImageTaskDistributor
import com.db.models.User
import com.models.TokenPayloadMapping
import com.services.{HttpClient, HttpClientBase}
import org.mongodb.scala.bson.collection.immutable.Document
import pdi.jwt.{Jwt, JwtAlgorithm, JwtHeader}
import com.services.auth.AuthService.{createToken, decodeToken, deserializeToken, isValidToken}
import com.typesafe.scalalogging.Logger
import com.models.EntityModels._
import org.mongodb.scala.MongoDatabase

import scala.util._
import scala.reflect.api.TypeTags
import scala.util.Try

trait RestRoutes extends imageDistributorApi with JsonSupport {
  private val conf = ConfigFactory.load()
  private val askTimeoutDuration = conf.getDuration("akka.ask-timeout-duration", TimeUnit.SECONDS)

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val t = Timeout(askTimeoutDuration, TimeUnit.SECONDS)
  implicit val exc = system.dispatcher
  implicit def httpClient: HttpClientBase = HttpClient()


  val apiVersion = conf.getString("api.version");

  val log = Logger(this.getClass)
  val appUrl =  "/"
//  implicit val db: MongoDatabase = Connection.getDatabase

  def setPath(path:String): String = apiVersion + path

  private def extractBearerToken(authHeader: Option[Authorization]): Option[String] =
    authHeader.collect {
      case Authorization(OAuth2BearerToken(token)) => token
    }



  def createFoton: Route = {
    val secret = conf.getString("app.auth.secretKey")
    val imageDistributor: ActorRef = system.actorOf(ImageTaskDistributor.props)

//    println(createToken(TokenPayloadMapping("ud", "carlos.config@gmail.com"), secret) +  "*****************************************************************")

    import akka.http.scaladsl.model.headers.{ Authorization, BasicHttpCredentials }

    def authenticated: Directive1[(StatusCode, String)] = {
      optionalHeaderValueByType(classOf[Authorization]).map(extractBearerToken).flatMap {
        case Some(token) if  isValidToken(token, secret) =>
          decodeToken(token, secret) match  {
            case Success(value) => println(value, "*" * 28); provide(StatusCodes.OK -> value)
            case Failure(e) =>  log.debug(e.toString) ; provide(StatusCodes.Unauthorized -> "Token is not Valid.")
          }
        case Some(token) if !isValidToken(token, secret) =>  provide(StatusCodes.Unauthorized -> "Token has expired or is not longer valid.")
      }
    }

    path("v1" /  "hello") {
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, ""))
          } ~
          post {
            authenticated { result =>
            implicit val tm = akka.util.Timeout(20 seconds)
              deserializeToken(result) match {
                case t: TokenPayloadMapping =>
                  entity(as[Foton]) { ft =>
                    onSuccess(Future { "Ok" }) { extraction =>
                      val searchResult =  httpClient.get(HttpUtils.getCustomSearchUrl(ft)).getFuture
//                      val _user = User()
                      //                imageDistributor ! CrashRequest(imgdistributor, "stop")
                      val searchResultF = searchResult flatMap  { response =>
                        Unmarshal(response.entity).to[String] map { t =>
                           Json.parse(t) \ "items" \\ "link"
                        }
                      }
//                        : Future[ImageTaskDistributorResponse]
//                     "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6ICJuYW1laWQiLCAiZW1haWwiOiAiY2FybG9zLmNvbmZpZ0BnbWFpbC5jb20ifQ.eV09hNdvmCzmaNU7eQQlFeeK33wHeG9PSCio-P2PFOY"
                        def getFutureFromActor(searchImages: Seq[JsValue]) = {

                           imageDistributor ! ProcessImages(searchImages)
                        }

                      for {
                          searchImages <- searchResultF
                         //  user <- _user.getUserByUsername(t.email)
                      } yield {
                        getFutureFromActor(searchImages)

                      }

                      complete("hello")


//                      onSuccess(searchResultF) {
//                        case s: Seq[JsValue] => {
//                          onSuccess(getFutureFromActor(s)) {
//                            case _: ImageTaskDistributorResponse => complete("hello")
//                          }
//                        }
//                      }
//                      for {
//                          searchImages <- searchResultF
////                          user <- _user.getUserByUsername(t.email)
//                      } yield  {
//
////                          user match {
////                            case u: Document =>
////                              println(searchImages + " routes" + "*" * 80, " size " + searchImages.length)
////                              imageDistributor ! ProcessImages(searchImages)
//
////                           (StatusCodes.OK, u.toString)
////                            case _ =>
////                              val msg = s"This user hasn't any associated account with us. Sign Up at $appUrl"
////                              (StatusCodes.BadRequest, msg)
////                          }
//                      }
//                      complete("asdf")
//                      complete(result)
                    }
                  }
              }
            }
          }
        }
  }
}


trait imageDistributorApi {}