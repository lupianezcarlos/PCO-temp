package com.db.models
import com.db.models.base.ModelsBase
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.model.Filters._

import scala.concurrent.Future

class Session(implicit db: MongoDatabase) extends ModelsBase {

  val collection: MongoCollection[Document] = db.getCollection("sessions")

  def getSession(id: ObjectId): Future[Document] = {
    collection.find(equal("_id", id)).first().toFuture()
  }

}

object Session {
  def apply()(implicit db: MongoDatabase): Session = new Session()
}
