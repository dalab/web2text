package nl.tvogels.boilerplate.database

import com.mongodb.casbah.Imports._

class Database(
    host: String = "localhost",
    port: Int = 27017,
    dbname: String = "boilerplate") {

  private val client = MongoClient(host, port)
  private val db     = client(dbname)

  private val Documents = db("documents")
  private val Datasets  = db("datasets")
  private val Labels    = db("labels")

  def createIndices(): Database = {
    Documents.createIndex(MongoDBObject("id"->1), MongoDBObject("unique"->true))
    Datasets.createIndex(MongoDBObject("id"->1), MongoDBObject("unique"->true))
    this
  }

  def insertDataset(id: String, name: String): Database = {
    Datasets.update(
      MongoDBObject("id" -> id),
      MongoDBObject("id" -> id, "name" -> name),
      upsert=true
    )
    this
  }

}