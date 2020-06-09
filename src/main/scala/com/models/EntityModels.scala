package com.models

object EntityModels {
  case class TokenPayload(id: String, email: String) extends ModelsBase
  case class Foton(q: String, num: Int, size: Option[String] = None) extends ModelsBase

}
