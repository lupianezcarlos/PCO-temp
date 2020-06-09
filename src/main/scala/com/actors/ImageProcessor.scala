package com.actors

import com.typesafe.config.{Config, ConfigFactory}
import akka.actor._
import akka.stream.ActorMaterializer
import com.sksamuel.scrimage.{Composite, Image}
import com.sksamuel.scrimage.composite.AverageComposite
import java.io.File
import java.nio.file.Path
import java.util.UUID.randomUUID

import com.actors.supervisors.ImageTaskDistributor.ActionPerformed
import com.actors.supervisors.config.ActorConfig

import scala.concurrent.{ExecutionContext, Future}

class ImageProcessor extends Actor with ActorConfig {

  implicit val system = context.system
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val conf:Config = ConfigFactory.load();

  val uploadPath = conf.getString("paths.uploadPath")

  import ImageProcessor._

  val imgHelper = new ImageHelper()

  var counter = 0;

  def receive: Receive = {
    case DownloadImages(imgs) => {
      imgs.foreach(img => {
        imgHelper.resizeImage(img, s"$uploadPath/${randomUUID.toString}.jpg")
      })

    }
//    case DownloadImage(img, name) =>
//       self ! ResizeImage(img, name)
//    case ResizeImage(img, name) =>
//      imgHelper.resizeImage(img, s"$uploadPath/$name.jpg")
//      if (end) self ! ComposeImages(uploadPath)

    case ComposeImages(`uploadPath`) =>
      println("composing")
        val uploads = new File(uploadPath).listFiles.filter(_.getName.contains("jpg")).sortBy(_.getName)
        val alphas = Seq(1, 0.9, 0.8, 0.6, 0.5)
        def compose(imgs: Seq[File], num: Int): Image = {
          val alpha: Double = if (imgs.size-1 - num <= 4)  alphas(imgs.size-1 - num) else 0.4
          if(num == 0) Image.fromFile(imgs(num))
          else {
            compose(imgs, num - 1).composite(new AverageComposite(0.5), Image.fromFile(imgs(num)))
          }
        }
      compose(uploads,uploads.size-1).output(new File(uploadPath + "/foton.jpg"))
      sender() ! ActionPerformed
  }
}


object ImageProcessor {
  def props:Props = Props[ImageProcessor]

  sealed trait ImageProcessorResponse
  case class DownloadImage(img: Array[Byte], name: String, end: Boolean) extends ImageProcessorResponse
  case class DownloadImages(images:  Seq[Array[Byte]]) extends ImageProcessorResponse
  case class ComposeImages(path: String) extends ImageProcessorResponse
  case class ResizeImage(img:  Array[Byte], name: String, end: Boolean) extends ImageProcessorResponse
  case object FotonCreated extends ImageProcessorResponse
  case object ImageProcessorFailed extends ImageProcessorResponse
  case object FotonResponse extends ImageProcessorResponse
}

class ImageHelper(implicit ec: ExecutionContext, mat: ActorMaterializer, system: ActorSystem) {
  def resizeImage(byteArray: Array[Byte], out: String):Path = {
    Image(byteArray)
      .cover(1000, 500)
      .output(out).getFileName
  }
}
