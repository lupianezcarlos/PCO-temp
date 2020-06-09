package com.actors.supervisors

import java.awt.image.BufferedImage
import java.io
import java.io.{File, InputStream}
import java.util.UUID.randomUUID

import akka.Done
import com.actors.ImageProcessor._
import com.actors.ImageProcessor
import com.actors.supervisors.ImageTaskDistributor.{ActionPerformed, ProcessImages, Response}
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.actors.supervisors.config.ActorConfig
import com.actors.supervisors.strategy.CrashRequest
import com.sksamuel.scrimage.Image
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import play.api.libs.json.JsValue
import com.actors.ImageProcessor._
import akka.pattern.ask
import com.api.CBreaker

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

class ImageTaskDistributor extends CBreaker with ActorConfig {
  import context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val mat: ActorMaterializer = ActorMaterializer()
  val conf:Config = ConfigFactory.load();

  val uploadPath = conf.getString("paths.uploadPath")


  private def createImageProcessor(id: String) : ActorRef = {  context.actorOf(ImageProcessor.props, s"image-processor-$id") }
//  private def downloadImage(img: Array[Byte], name: String, processor: ActorRef): Unit =  processor ! DownloadImage(img, name)

  def receive: Receive = {
    case ProcessImages(images) =>
//      val imageProcessor = createImageProcessor(randomUUID.toString)
      def handleErrors(responses: Seq[Future[HttpResponse]]) = responses.map(_.recover {
        case NonFatal(e) => HttpResponse(StatusCodes.BadRequest)
      })

      val uploadPath = conf.getString("paths.uploadPath")

      def downloadImages(images: Seq[String]): Future[Seq[(StatusCode, ByteString)]] = {

        def getRequests(uri: String): Future[HttpResponse] = {
//          breaker.withCircuitBreaker(Http().singleRequest(HttpRequest(uri = uri)))
          Http().singleRequest(HttpRequest(uri = uri))
        }

        val imagesResponses = Future.sequence(images.map(uri => getRequests(uri)))
        imagesResponses map { seqOfResponses =>
           Future.sequence(seqOfResponses.map {
              case HttpResponse(StatusCodes.OK, headers, entity, _) =>
                  entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body =>
                    println("ok " + " * " * 800)
                    (StatusCodes.OK, body)
                  }
              case resp@HttpResponse(code, _, _, _) =>
                 Future.successful((code, ByteString("")))
              case _ =>  println("okddd " + " * " * 800); Future.successful((StatusCodes.OK, ByteString("")))
            })
        }
      }.flatten

      downloadImages(images.map(_.as[String])).map { images =>
         println("images " + " * " * 800)
        images.map {
          case (code, byteString) =>println("goooddd " + " * " * 800); Image(byteString.toArray).output(s"$uploadPath/1.jpg")
          case _ => println("wrong " + " * " * 800)
        }
      }
      Thread.sleep(2000)
//      def downloadImages(images: Seq[String]): Future[Done] = {
//        val requestResponseFlow = Http().superPool[Long]()
//        Source.fromIterator(() => images.iterator)
//          .zipWithIndex.map(i => (HttpRequest(uri = i._1), i._2))
//          .via(requestResponseFlow)
//          .mapAsync(10) {
//            case (Success(value), i) =>
//              value match {
//                case HttpResponse(StatusCodes.OK, headers, entity, _) =>
//                  entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(body => (body, i))
//                }
//            case (Failure(e), _) => Future.failed(throw new Exception("wrong"))
//          }.runWith(Sink.foreach {
//          case (value:  akka.util.ByteString, i) =>
//            Image(value.toArray) match {
//              case img: Image => img.cover(1000, 1000).output(s"$uploadPath/$i.jpg"); println("here");
//              case _  => println(s"An error downloading the images happened")
//            }
//          case _ => Done
//        })
//      }


//      downloadImages(images.map(_.as[String])).map {
//        case f: Future[Done] => (imageProcessor ask ComposeImages(uploadPath)).mapTo[ImageProcessorResponse]
//        case _  => Future.successful(ImageProcessorFailed)
//      }
    case CrashRequest(ref, how) => ref ! CrashRequest(ref, how)
    case msg => println(msg + " <= message received")
  }
}


object ImageTaskDistributor   {

  sealed trait ImageTaskDistributorResponse

  case class ImageCreated(image: File) extends ImageTaskDistributorResponse

  case class GetImagesList(images: Seq[String]) extends ImageTaskDistributorResponse

  case class CropImage(buffImg: BufferedImage) extends ImageTaskDistributorResponse

  case class ProcessImages(images: Seq[JsValue]) extends ImageTaskDistributorResponse

  case object ActionPerformed extends ImageTaskDistributorResponse

  case object Response extends ImageTaskDistributorResponse

  def props: Props = Props[ImageTaskDistributor]

}