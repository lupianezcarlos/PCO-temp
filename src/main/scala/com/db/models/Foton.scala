package com.db.models
import com.db.models.base.ModelsBase
import org.mongodb.scala.MongoDatabase

class Foton(implicit db: MongoDatabase) extends ModelsBase {

  val collection: MongoCollection[Document] = db.getCollection("fotones")
}


object Foton {
  def apply()(implicit db: MongoDatabase): Foton = new Foton()
}