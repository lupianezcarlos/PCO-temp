package com.api

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.models.EntityModels._


trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val fotonFormat = jsonFormat3(Foton)
  implicit val payloadFormat = jsonFormat2(TokenPayload.apply)
//  implicit val responseFormat = jsonFormat2(ToResponse)

}