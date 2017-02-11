package nl.tvogels.boilerplate.utilities

import edu.knowitall.cluewebextractor.{WarcRecordIterator,WarcRecord}
import java.io.{File, FileInputStream, DataInputStream, BufferedInputStream}
import java.util.zip.GZIPInputStream

/** Utility for reading WARC files
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
object Warc {

  /** Create an iterator, given a WARC file */
  def iteratorForFile(fileName: String) = {
    val f = new File(fileName)
    new WarcRecordIterator(new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(f)))))
  }

  /** Split a WarcRecord into headers and content */
  def headersAndContent(warc: WarcRecord) = {
    val fileFormat = """(?s)(.*?)?(?:\r?\n|\r){2,}+[\n\r]*(.*)""".r
    warc.payload match {
      case fileFormat(headers, content) => (headers, content)
    }
  }

}