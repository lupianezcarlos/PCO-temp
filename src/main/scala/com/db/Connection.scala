package com.db
import org.mongodb.scala.MongoDatabase

object Connection {
  import scala.collection.JavaConverters._

  import com.typesafe.config.ConfigFactory
  import com.typesafe.config.Config

  import org.bson.UuidRepresentation
  import org.bson.codecs.UuidCodec
  import com.mongodb.{MongoClientSettings, MongoCredential, ServerAddress}
  import org.mongodb.scala.{Completed, Document, MongoClient, MongoCollection, Observable, Observer}
  import org.bson.codecs.configuration.{CodecRegistries, CodecRegistry}

  val config: Config = ConfigFactory.load()

  private val user: String = config.getString("db.user")                       // the user name
  private val source: String =  config.getString("db.name")                 // the source where the user is defined
  private val password: Array[Char] = config.getString("db.password") .toCharArray
  private val credential: MongoCredential = MongoCredential.createCredential(user, source, password)

  // Replaces the default UuidCodec with one that uses the new standard UUID representation
  val codecRegistry: CodecRegistry =
    CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
      MongoClient.DEFAULT_CODEC_REGISTRY)

  val settings: MongoClientSettings = MongoClientSettings.builder()
    .applyToClusterSettings(b => b.hosts(List(new ServerAddress(config.getString("db.host"))).asJava).description("Mlab Server"))
    .credential(credential)
    .codecRegistry(codecRegistry)
    .build()

  val client: MongoClient = MongoClient(settings)

  def getDatabase: MongoDatabase = client.getDatabase(source)
}

