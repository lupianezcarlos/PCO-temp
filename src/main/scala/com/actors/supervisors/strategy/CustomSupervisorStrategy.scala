package com.actors.supervisors.strategy

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, AllForOneStrategy, OneForOneStrategy, Props}
import akka.actor.SupervisorStrategy._

import scala.concurrent.duration._

trait CustomSupervisorStrategy {


  def one = OneForOneStrategy() {
    case _: RestartException => Restart
    case _: ResumeException => Resume
    case _: StopException => Stop
  }

  def all =   AllForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10.seconds) {
    case _: RestartException => Restart
    case _: ResumeException => Resume
    case _: StopException => Stop
    case _: Exception => Escalate
  }

  def crash(how: String): Unit = {
    how match {
      case "restart" => throw new RestartException
      case "resume" => throw new ResumeException
      case "stop" => throw new StopException
      case _ => throw new Exception
    }
  }

}

case class CrashRequest(ref: ActorSelection, how: String)
case class RestartException(reason: String = "RESTART") extends Exception(reason)
case class ResumeException(reason: String = "RESUME") extends Exception(reason)
case class StopException(reason: String = "STOP") extends Exception(reason)