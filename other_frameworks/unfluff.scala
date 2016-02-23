import java.io.FileReader
import de.l3s.boilerpipe.extractors._

import nl.tvogels.boilerplate.cleaneval._
import nl.tvogels.boilerplate.alignment._
import nl.tvogels.boilerplate.utilities._

object Unfluff {

  def main(args: Array[String]): Unit = {
    alignPages
  }

  /** Step 1: Generate the prediction files, which the `unfluff/unfluff.js` command line tool.
    * Make sure to set the right in / output directories */

  /** Step 2: Align the files and save them in the `output` dir as well */
  def alignPages = {
    val folder = "unfluff"
    for (page <- CleanEval.iterator) {
      if (true) {
        val orig = page.source
        val clean = Util.loadFile(s"output/$folder/${page.id}.html").trim
        if (!Util.fileExists(s"output/$folder/${page.id}-aligned.txt")) {
          val alignment = Alignment.alignment(orig, clean)
          Util.save(s"output/$folder/${page.id}-aligned.txt", alignment)
        }
        println(s"Done with ${page.id}")
      }
    }
  }

}