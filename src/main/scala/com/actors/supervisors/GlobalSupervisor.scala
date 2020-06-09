package com.actors.supervisors

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.actors.supervisors.ImageTaskDistributor.ProcessImages
import com.actors.supervisors.strategy.CrashRequest
import play.api.libs.json.JsValue

class GlobalSupervisor()(implicit val timeout: Timeout) extends Actor {

  import GlobalSupervisor._

  override def receive: Receive = {
    case CrashRequest(ref, how) => ref ! CrashRequest(ref, how)
    case CreateImageTaskDistributor(searchImages, name) =>
      val imageTaskDistributor = context.actorOf(ImageTaskDistributor.props, name.getOrElse(randomUUID().toString))
      imageTaskDistributor ! ProcessImages(searchImages)
  }
}


object GlobalSupervisor {

  def getImageTaskDistributorName(id: String) = "image-task-distributor"
	case class CreateImageTaskDistributor(searchImages: Seq[JsValue], name: Option[String])
  case object ActionPerformed

	def props(implicit timeout:Timeout):Props = Props(new GlobalSupervisor)

}

trait GlobalActor {

  def imageTaskDistributor: ActorRef
	println("GLOBAL ACTOR TRAIT")
}
