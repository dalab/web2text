package nl.tvogels.boilerplate

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.cleaneval.CleanEval
import org.jsoup.Jsoup

object Main {
  def main(args: Array[String]): Unit = {

    val arr: Vector[Double] = Vector(1,2,5,3,26,8,6,3)
    println(Util.findKMedian(arr,5))

  }


  def alignCleanEvalData =
    CleanEval.generateAlignedFiles("/Users/thijs/dev/boilerplate2/src/main/resources/cleaneval/aligned")
}