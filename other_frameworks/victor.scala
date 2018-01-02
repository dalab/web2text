import java.io.FileReader
import de.l3s.boilerpipe.extractors._
import org.jsoup.Jsoup

import ch.ethz.dalab.web2text.cleaneval._
import ch.ethz.dalab.web2text.alignment._
import ch.ethz.dalab.web2text.utilities._

object Victor {

  def main(args: Array[String]): Unit = {
    alignPages
  }

  /** Step 1: extracts text from pages cleaned by victor */
  def cleanPages = {

    for (page <- CleanEval.iterator) {
      val filename = s"output/victor/${page.id}.clean.html"
      if (Util.fileExists(filename)) {
        val text = Util.loadFile(filename)
        val dom = Jsoup.parse(text)
        println(s"Removing ${dom.body.select(".victor_other").size} elements.")
        dom.body.select(".victor_other").remove()
        Util.save(s"output/victor/${page.id}.txt",dom.body.text)
      }
    }

  }

  /** Step 2: Align the files and save them in the `output` dir as well */
  def alignPages = {
    for (page <- CleanEval.iterator) {
      val filename = s"output/victor/${page.id}.txt"
      if (Util.fileExists(filename)) {
        val orig = page.source
        val clean = Util.loadFile(filename)
        if (!Util.fileExists(s"output/victor/${page.id}-aligned.txt")) {
          val alignment = Alignment.alignment(orig, clean)
          Util.save(s"output/victor/${page.id}-aligned.txt", alignment)
        }
        println(s"Done with ${page.id}")
      }
    }
  }

}
