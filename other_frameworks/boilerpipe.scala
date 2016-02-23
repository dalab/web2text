import java.io.FileReader
import de.l3s.boilerpipe.extractors._

import nl.tvogels.boilerplate.cleaneval._
import nl.tvogels.boilerplate.alignment._
import nl.tvogels.boilerplate.utilities._

object Boilerpipe {

  def main(args: Array[String]): Unit = {
    alignPages
  }

  /** Step 1: cleans the pages and saves them in the `output` dir */
  def cleanPages = {

    for (page <- CleanEval.iterator) {
      val text = page.source
      val cleaned = LargestContentExtractor.INSTANCE.getText(text);
      Util.save(s"output/largestcontent-extractor/${page.id}-clean.txt", cleaned)
      val cleaned2 = ArticleExtractor.INSTANCE.getText(text);
      Util.save(s"output/article-extractor/${page.id}-clean.txt", cleaned2)
      val cleaned3 = DefaultExtractor.INSTANCE.getText(text);
      Util.save(s"output/default-extractor/${page.id}-clean.txt", cleaned3)
      println(s"Done with ${page.id}")
    }

  }

  /** Step 2: Align the files and save them in the `output` dir as well */
  def alignPages = {
    for(folder <- List("largestcontent-extractor","default-extractor","article-extractor")) {
      for (page <- CleanEval.iterator) {
        val filename = s"output/$folder/${page.id}-clean.txt"
        if (Util.fileExists(filename)) {
          val orig = page.source
          val clean = Util.loadFile(filename)
                        .trim
          if (!Util.fileExists(s"output/$folder/${page.id}-aligned.txt")) {
            val alignment = Alignment.alignment(orig, clean)
            Util.save(s"output/$folder/${page.id}-aligned.txt", alignment)
          }
          println(s"Done with ${page.id}")
        }
      }
    }
  }

}