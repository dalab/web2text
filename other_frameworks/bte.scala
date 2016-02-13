import java.io.FileReader
import de.l3s.boilerpipe.extractors._

import nl.tvogels.boilerplate.cleaneval._
import nl.tvogels.boilerplate.alignment._
import nl.tvogels.boilerplate.utilities._

object BTE {

  def main(args: Array[String]): Unit = {

  }

  /** Step 1: Generate the prediction files, which the `bte/bte.py` command line tool.
    *         It produces output in the source directory (234.html > 234.txt) */

  /** Step 2: Align the files and save them in the `output` dir as well */
  def alignPages = {
    val folder = "bte"
    for (page <- CleanEval.iterator) {
      val orig = page.origWithoutTextTag
      val clean = Util.loadFile(s"output/$folder/${page.id}.txt")
                    .toUpperCase
                    .replaceAll("""\s+"""," ")
                    .trim
      if (!Util.fileExists(s"output/$folder/${page.id}-aligned.txt")) {
        val alignment = Alignment.alignment(orig, clean)
        Util.save(s"output/$folder/${page.id}-aligned.txt", alignment)
      }
      println(s"Done with ${page.id}")
    }
  }

}