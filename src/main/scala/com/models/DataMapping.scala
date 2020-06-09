package com.models
import play.api.libs.json._


  case class TokenPayloadMapping(id: String, email: String) extends MappingBase {
    def toJson: String = s"""{"id": "$id", "email": "$email"}"""
  }

  object TokenPayloadMapping extends MappingBase

  case class MappingError(msg: String) extends MappingBase


trait MappingBase {
  implicit val format: OFormat[TokenPayloadMapping] = Json.format[TokenPayloadMapping]
}