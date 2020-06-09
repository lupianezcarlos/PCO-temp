package com.db.models.base

trait ModelsBase {
  type MongoCollection[A] =  org.mongodb.scala.MongoCollection[A]
  type Document = org.mongodb.scala.Document
  type ObjectId = org.mongodb.scala.bson.BsonObjectId

  object ObjectId {  def apply(id: String) = org.mongodb.scala.bson.BsonObjectId(id) }


  val collection: MongoCollection[Document]
}
