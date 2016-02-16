package nl.tvogels.boilerplate.visualization

import com.mongodb.casbah.Imports._
import nl.tvogels.boilerplate.cleaneval.CleanEval
import nl.tvogels.boilerplate.cdom.{DOM,CDOM}
import nl.tvogels.boilerplate.alignment.Alignment
import org.jsoup.Jsoup

/**
 * Visualization functionality
 *
 * Functionality for saving labels and blocks to
 */
case class Visualization(port: Int) {

  /** MongoDB Client */
  private val mongoClient = MongoClient("localhost", port)

  /** MongoDB db connection */
  val db = mongoClient("boilerplate")
  val Pages = db("pages")
  val Groups = db("groups")

  def storeCleanEvalInMongo = {

    Groups.update(
      MongoDBObject("id"->"cleaneval"),
      MongoDBObject("id"->"cleaneval","name"->"CleanEval 2007"),
      upsert=true
    )

    for (page <- CleanEval.iterator) {

      println(s"Working on page ${page.id} for server on port $port.")

      val dom = Jsoup.parse(page.orig,page.url)
      val cdom = CDOM.fromBody(dom.body)
      DOM.wrapBlocks(dom)
      DOM.removeJavascript(dom)
      DOM.inactivateLinks(dom)
      DOM.makeUrlsAbsolute(dom)

      assert(
        page.aligned.length == page.orig.length,
        s"Lenghts are unequal for page ${page.id}"
      )
      val groundtruth = Alignment.labelsFromAlignedString(cdom, page.aligned)

      val nBlocks = dom.getElementsByClass("boilerplate-text-block").size

      assert(groundtruth.length == nBlocks)

      val query = MongoDBObject("doc_id" -> page.docId)

      val update = MongoDBObject(
        "doc_id" -> page.docId,
        "group" -> "cleaneval",
        "source_code" -> dom.toString(),
        "original" -> page.orig,
        "cleaned" -> page.clean,
        "aligned" -> page.aligned,
        "url" -> page.url,
        "encoding" -> page.encoding,
        "n_blocks" -> nBlocks,
        "labels" -> MongoDBObject(
          "ground_truth" -> groundtruth
        )
      )

      Pages.update( query, update, upsert=true )
    }
  }

  def addLabelsToDocument(labelName: String, labels: Seq[Int], docId: String) = {

      val query = MongoDBObject("doc_id" -> docId)
      Pages.update(query, $set(s"labels.$labelName" -> labels))
  }

}