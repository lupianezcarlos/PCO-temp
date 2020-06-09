package com.utils

import akka.http.javadsl.model.headers.ContentDisposition
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http

import scala.concurrent.Future
import scala.util.Try
import java.io._

import akka.Done
import com.actors.supervisors.ImageTaskDistributor._
import akka.pattern.ask

import scala.concurrent.duration._
import akka.util.Timeout
import com.actors.supervisors.ImageTaskDistributor

class FileUtil(implicit materializer:ActorMaterializer, system: ActorSystem ) {
  implicit val timeout: Timeout = 1 seconds

     val p = new com.utils.Path()

      def destinationFile(downloadDir: File, response: HttpResponse): File = {
        val fileName = response.header[ContentDisposition].get.value
        val file = new File(downloadDir, fileName)
        file.createNewFile()
        file
      }

      def writeFile(downloadDir : File)(httpResponse : HttpResponse) : Future[IOResult] = {
          val file = destinationFile(downloadDir, httpResponse)
          httpResponse.entity.dataBytes.runWith(FileIO.toFile(file))
      }

      def responseOrFail[T](in: (Try[HttpResponse], T)): (HttpResponse, T) = in match {
        case (responseTry, context) => (responseTry.get, context)
      }

      def downloadViaFlow2(uri: Uri, downloadDir: String) : Future[Done] = {
        var dd = new File(downloadDir)
        val request = HttpRequest(uri=uri)
        val source = Source.single((request, ()))
        val requestResponseFlow = Http().superPool[Unit]()

        source.via(requestResponseFlow)
          .map(responseOrFail)
          .map(_._1)
          .runWith(Sink.foreach(writeFile(dd)))
      }

      def downloadFile(uri:play.api.libs.json.JsValue, destination:String) = {
        import java.nio.channels._
        import java.io._
        import java.net.URL

        try {
              val url = new URL(uri.as[String])
              val readableByteChannel:ReadableByteChannel = Channels.newChannel(url.openStream());
              val fileOutputStream:FileOutputStream = new FileOutputStream(destination);
              val fileChannel:FileChannel = fileOutputStream.getChannel();
              fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MaxValue);
            } catch {
               case e: IOException => println(e)
               case _: Throwable => "Something when wrong downloading file"
            }
      }

     def downloadFiles(urls:Seq[play.api.libs.json.JsValue],destinationDir:String, ext:String = "jpg") = {
             for( i <- urls.indices ) { downloadFile(urls(i), p.join(destinationDir,s"$i.$ext")) }
     }

}

object FileUtil {
  def apply()(implicit materializer: ActorMaterializer, system: ActorSystem) = new FileUtil()
}