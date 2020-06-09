package  com.routes

//import java.util.concurrent.TimeUnit
//
//import akka.actor.{ Actor, ActorLogging, Props }
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.Http.ServerBinding
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.model.headers.RawHeader
//import akka.http.scaladsl.server.{ Directive1, Route }
//import akka.http.scaladsl.server.Directives._
//import akka.stream.ActorMaterializer
//import akka.pattern._
//
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//import io.circe.generic.auto._
//import authentikat.jwt._
//
//final case class LoginRequest(username: String, password: String)

class Auth {
//  private def login = post {
//
//
//    //example of authentication route
//    entity(as[LoginRequest]) {
//      case lr @ LoginRequest("admin", "admin") =>
//        val claims = setClaims(lr.username, tokenExpiryPeriodInDays)
//        respondWithHeader(RawHeader("Access-Token", JsonWebToken(header, claims, secretKey))) {
//          complete(StatusCodes.OK)
//        }
//      case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
//    }
//  }
//
//  private def securedContent = get {
//    authenticated { claims =>
//      complete(s"User ${claims.getOrElse("user", "")} accessed secured content!")
//    }
//  }
//
//  private def authenticated: Directive1[Map[String, Any]] =
//    optionalHeaderValueByName("Authorization").flatMap {
//      case Some(jwt) if isTokenExpired(jwt) =>
//        complete(StatusCodes.Unauthorized -> "Token expired.")
//
//      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
//        provide(getClaims(jwt).getOrElse(Map.empty[String, Any]))
//
//      case _ => complete(StatusCodes.Unauthorized)
//    }
//
//  private def setClaims(username: String, expiryPeriodInDays: Long) = JwtClaimsSet(
//    Map("user" -> username,
//      "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
//        .toMillis(expiryPeriodInDays)))
//  )
//
//  private def getClaims(jwt: String) = jwt match {
//    case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption
//    case _                          => None
//  }
//
//  private def isTokenExpired(jwt: String) = getClaims(jwt) match {
//    case Some(claims) =>
//      claims.get("expiredAt") match {
//        case Some(value) => value.toLong < System.currentTimeMillis()
//        case None        => false
//      }
//    case None => false
//  }
//
//  def apply() = new Auth()

}

object Auth {
  import Auth._
}
