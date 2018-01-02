package ch.ethz.dalab.web2text.cleaneval

import ch.ethz.dalab.web2text.utilities.Util.codec
import scala.io.{Source,Codec}

/** Case class for a CleanEval page containing all available information on it.
  * @param id ID
  */
case class Page(id: Integer) {

  /** Unique ID across datasets */
  val docId = f"cleaneval_$id%03d"

  private val firstLine = CleanEval.loadFirstLine(id)

  def charsetMap(given: String) = given.toLowerCase match {
    case "windows-1252" => "windows-1252"
    case "iso-8859-1" => "iso-8859-1"
    case "utf-8" => "utf-8"
    case "utf8" => "utf-8"
    case _ => ""
  }

  private val UrlTitleEncoding = """(?i)<text id="(.*?)" title="(.*?)" encoding="(.*?)">""".r
  private val UrlEncoding = """(?i)<text id="(.*?)" encoding="(.*?)">""".r

  val (url: String, title: String, encoding: String) = firstLine match {
    case UrlTitleEncoding(url, title, encoding) =>
      (url, title, charsetMap(encoding))
    case UrlEncoding(url, encoding) =>
      (url, "", charsetMap(encoding))
    case _ => println("NO MATCH "+firstLine); ("","","")
  }

  /** Original file contents, not including the first and last lines,
    * which have <text blabla></text> */
  lazy val source: String = CleanEval.loadOrigFile(id)

  lazy val clean: String = CleanEval.loadCleanFile(id)

  lazy val aligned: String = CleanEval.loadAlignedFile(id)

}
