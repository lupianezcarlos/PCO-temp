package com.utils

import scala.util.matching.Regex
import java.nio.file.Paths


class Path {

  private val currentRelativePath:java.nio.file.Path  = Paths.get("")
  def  __dirname:String = currentRelativePath.toAbsolutePath().toString

  def join(d:String, p:String) = {
    val leftWithSlash = new Regex("[\\w/]+\\/$")
    val rightWithSlash = new Regex("^/.*")
    val normalizedPath = (d, p) match {
      case (leftWithSlash(),rightWithSlash()) => d + p.drop(1)
      case _ => d + "/" +  p
    }
    println(normalizedPath,  " 88 *********************")
    normalizedPath.replaceFirst("//","/")
  }
}
