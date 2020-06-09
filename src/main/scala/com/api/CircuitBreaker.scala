package com.api

import scala.concurrent.duration._
import akka.pattern.CircuitBreaker
import akka.pattern.pipe
import akka.actor.{ Actor, ActorLogging, ActorRef }

trait CBreaker extends Actor with ActorLogging {
  import context.dispatcher

  val breaker = new CircuitBreaker(context.system.scheduler, maxFailures = 5, callTimeout = 5.seconds, resetTimeout = 1.minute)
      .onOpen(notifyMeOnOpen())

  def notifyMeOnOpen(): Unit =
    log.warning("My CircuitBreaker is now open, and will not close for one minute")

}



