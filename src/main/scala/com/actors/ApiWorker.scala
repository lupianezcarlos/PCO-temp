//package actors
//
//import akka.actor.{Actor, Props}
//
//case class Fired(name: String)
//
//class ApiWorker extends Actor {
//
//
//  def receive:Receive = {
//     case Fired(value) => sender value
//     case _ =>
//   }
//}
//
//object ApiWorker {
//  def props:Props = Props(new ApiWorker)
//}
