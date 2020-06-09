package com.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.http.impl.model.parser.UriParser
import com.models.ModelsBase
import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
import play.api.libs.json._

object HttpUtils {
    private val config = ConfigFactory.load();

    def getQueryString(entity:ModelsBase) = {
        val keyValue = entity.getClass.getDeclaredFields.toList zip entity.productIterator.toList
        val queryPair = for {(key, value) <- keyValue} yield ( key.getName,value)
           queryPair.map { _ match {
              case (key,Some(value)) => "%s=%s".format(key, URLEncoder.encode(s"$value"))
              case (key,None)  => None
              case (key,value) => "%s=%s".format(key, URLEncoder.encode(s"$value"))
            }}.filter(x => !x.equals(None)).mkString("&","&","")
      }

      def getCustomSearchUrl(dm:ModelsBase) = {
        val urlConfig = List(config.getString("googleSearch.key"),config.getString("googleSearch.cx")).mkString("&","&","")
        config.getString("googleSearch.base") + urlConfig + this.getQueryString(dm)
      }

      def errorMsg(msg: String): JsObject = {
        Json.obj("status" -> "Ok", "error" -> msg)
      }

      def get[A](a: String)(implicit reads: Reads[A], writes: Writes[A]): JsResult[JsValue] = {
        Json.parse(a).validate[A] match {
          case JsSuccess(jsn, _) => JsSuccess(Json.toJson(jsn))
          case _: JsError => JsError("An error happened validating json")
        }
      }

}