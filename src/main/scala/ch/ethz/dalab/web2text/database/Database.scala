package ch.ethz.dalab.web2text.database

import ch.ethz.dalab.web2text.cdom.DOM
import com.mongodb.casbah.Imports._
import org.jsoup.Jsoup

/** MongoDB client for the labeling server */
class Database(
    host: String = "localhost",
    port: Int = 27017,
    dbname: String = "boilerplate") {

  private val client = MongoClient(host, port)
  val db             = client(dbname)

  private val Documents = db("documents")
  private val Datasets  = db("datasets")
  private val Labels    = db("labels")

  def createIndices(): Database = {
    val props = MongoDBObject("unique"->true)
    Documents.createIndex(MongoDBObject("doc_id"->1), props)
    Datasets.createIndex(MongoDBObject("id"->1), props)
    Labels.createIndex(MongoDBObject("doc_id"->1,"label_name"->1), props)
    Labels.createIndex(MongoDBObject("dataset"->1,"label_name"->1))
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

  def insertDocument(
    dataset: String,
    docId: String,
    source: String,
    url: String,
    encoding: String,
    metadata: Map[String, AnyRef] = Map()
  ) = {
    val dom = makePageBlockedAndSafe(source, url)
    val query  = MongoDBObject("doc_id" -> docId)
    val update = MongoDBObject(
      "doc_id"          -> docId,
      "dataset"         -> dataset,
      "source"          -> source,
      "blocked_source"  -> dom.toString,
      "url"             -> url,
      "encoding"        -> encoding,
      "n_blocks"        -> nBlocks(dom),
      "metadata"        -> MongoDBObject(metadata.toList : _*)
    )
    Documents.update( query, update, upsert=true )
  }

  def insertLabels(
    docId: String,
    dataset: String,
    labelName: String,
    labels: Seq[Int],
    userGenerated: Boolean = false,
    metadata: Map[String, AnyRef] = Map()
  ) = {
    val update = MongoDBObject(
      "doc_id"          -> docId,
      "dataset"         -> dataset,
      "label_name"      -> labelName,
      "labels"          -> labels,
      "user_generated"  -> userGenerated,
      "metadata"        -> MongoDBObject(metadata.toList : _*)
    )
    val query = MongoDBObject(
      "doc_id"          -> docId,
      "label_name"      -> labelName
    )
    Labels.update( query, update, upsert=true )
  }

  def getLabels(dataset: String, labelName: String): Map[String, Vector[Int]] = {

    val res = Labels.find(
      MongoDBObject("dataset" -> dataset, "label_name" -> labelName),
      MongoDBObject("labels"->1, "doc_id"->1, "_id"->0)
    )

    val things = res map { doc =>
      (
        doc.get("doc_id").asInstanceOf[String],
        doc.get("labels").asInstanceOf[com.mongodb.BasicDBList]
                         .toVector.map(_.asInstanceOf[Int])
      )
    }
    things.toMap
  }

  private def makePageBlockedAndSafe(source: String, url: String): org.jsoup.nodes.Element = {
    val dom = Jsoup.parse(source,url)
    DOM.wrapBlocks(dom)
    DOM.removeJavascript(dom)
    DOM.inactivateLinks(dom)
    DOM.makeUrlsAbsolute(dom)
    dom
  }

  private def nBlocks(dom: org.jsoup.nodes.Element) =
    dom.getElementsByClass(DOM.BLOCK_CLASS).size


}
