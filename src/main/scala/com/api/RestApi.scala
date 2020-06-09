package com.api

import java.util.Calendar
import java.util.concurrent.TimeUnit

import com.services.HttpClient
import akka.actor.{ActorRef, ActorSystem}

import scala.util.{Failure, Success}
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.routes.RestRoutes
import com.services.HttpClient
import com.utils.FileUtil
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.actors.supervisors.ImageTaskDistributor
import com.typesafe.config.ConfigFactory

class RestApi() extends RestRoutes with JsonSupport {

  def routes: Route = createFoton

}