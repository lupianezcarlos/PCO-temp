package com.actors.supervisors.config


import akka.actor.ActorRef
import play.api.libs.json._

trait ActorConfig {
  import scala.concurrent.duration._
  import akka.util.Timeout
  implicit val timeout = Timeout(20 seconds)

}
