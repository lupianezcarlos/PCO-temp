package com.db.models

import com.db.models.base.ModelsBase
import org.mongodb.scala.{MongoClient, MongoDatabase}
import org.mongodb.scala.model.Filters._

import scala.concurrent.Future

class User(implicit db: MongoDatabase) extends ModelsBase {

  val collection: MongoCollection[Document] = db.getCollection("users")

  /**
  *  Get user by user email
    * @param email The email of the user
    * @return Document
    */
  def getUserByUsername(email: String): Future[Document] = {
    collection.find(equal("email", email)).first().toFuture()
  }

  /**
  * Get User by Id
    * @param id The objectId of the user
    * @return Document
    */
  def getUserById(id: ObjectId): Future[Document] = {
    collection.find(equal("_id", id)).first().toFuture()
  }

//  def userLogin(): Future[Document] = {
//
//  }
// insert a document with observable example
//  val document: Document = Document("x" -> 1)
//  val insertObservable: Observable[Completed] = collection.insertOne(document)
//                insertObservable.subscribe(new Observer[Completed] {
//                  override def onNext(result: Completed): Unit = println(s"onNext: $result *************************")
//                  override def onError(e: Throwable): Unit = println(s"onError: $e ***********************************")
//                  override def onComplete(): Unit = println("onComplete *****************************************8")
//                })

}

object User {
  def apply()(implicit db: MongoDatabase): User = new User()
}
