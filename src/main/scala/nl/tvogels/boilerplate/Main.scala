package nl.tvogels.boilerplate

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.cleaneval.CleanEval
import jaligner.{Alignment => JAlignment,Sequence,SmithWatermanGotoh}

object Main {
  def main(args: Array[String]): Unit = {
    CleanEval.generateAlignedFiles("/Users/thijs/dev/boilerplate2/src/main/resources/cleaneval/aligned")

  }
}