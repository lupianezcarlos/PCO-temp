package com.utils

import akka.http.scaladsl.model.HttpRequest


class CookiesUtil {

  def getCookie(name:String)(implicit request:HttpRequest):String = ???

}