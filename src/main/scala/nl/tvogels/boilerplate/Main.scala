package nl.tvogels.boilerplate

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.cleaneval.CleanEval
import org.jsoup.Jsoup

object Main {
  def main(args: Array[String]): Unit = {

    val doc = CleanEval.iterator.next()
    val body = Jsoup.parse(doc.orig)
    println(body)

  }


  def alignCleanEvalData =
    CleanEval.generateAlignedFiles("/Users/thijs/dev/boilerplate2/src/main/resources/cleaneval/aligned")
}