package com.services.auth

import akka.http.scaladsl.model.{HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity}
import com.api.JsonSupport
import com.exceptions.TokenValidationException
import com.models.EntityModels.Foton
import com.models.{MappingBase, _}
import com.utils.HttpUtils
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions}
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success, Try}
import spray.json._
import DefaultJsonProtocol._


object AuthService extends JsonSupport  {
  def createToken(user: TokenPayloadMapping, secret: String, exp: Option[Long] = None): String = {
    if(exp.isDefined) {
      println(user.toJson, "*"*80)
      Jwt.encode(JwtClaim(user.toJson).issuedNow.expiresIn(exp.getOrElse(-1)), secret, JwtAlgorithm.HS256)
    } else {
      println(user.toJson, "*"*80)
      Jwt.encode(user.toJson, secret, JwtAlgorithm.HS256)
    }
  }

  def isValidToken(token: String, secret: String) = {
     Jwt.isValid(token, secret, Seq(JwtAlgorithm.HS256))
  }

  import play.api.libs.json._

  def decodeToken(token: String, secret: String): Try[String] = {
     Jwt.decode(token, secret, Seq(JwtAlgorithm.HS256))  match {
       case Success(value) =>
         Json.parse(value).validate[TokenPayloadMapping] match {
           case JsSuccess(_, _) => Success(value)
           case _: JsError => Failure(new TokenValidationException("Json validation error for TokenPayload. Make sure the data is correct."))
         }
       case Failure(_) =>  Failure(new TokenValidationException("An error decoding the token happened"))
     }
  }

  import akka.http.scaladsl.server.Directives._

  def deserializeToken(result: (StatusCode, String)): MappingBase = {
      result match {
        case (StatusCodes.OK, token) =>
           HttpUtils.get[TokenPayloadMapping](token) match {
             case JsSuccess(value, _) => value.as[TokenPayloadMapping]
             case e: JsError => MappingError((JsError toJson(e)).toString())
           }
        case (st @ StatusCodes.Unauthorized, errorMsg) =>
           MappingError("This user is not authorized to access this page.")
        case _ =>
//          log
          MappingError("An Error happened converting the token.")
      }
  }
}
