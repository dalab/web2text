package ch.ethz.dalab.web2text

import scala.io.Source
import breeze.linalg.csvwrite
import ch.ethz.dalab.web2text.cdom.CDOM
import ch.ethz.dalab.web2text.features.extractor._
import ch.ethz.dalab.web2text.features.FeatureExtractor
import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.output.CleanTextOutput

/**
 * This is the second step in classifying boilerplate content in a webpage.
 * It takes:
 * (1) input html file
 * (2) csv file with block labels, produced by [python main.py classify] after running ExtractPageFeatures
 * (3) output html file
 */
object ApplyLabelsToPage {

  def main(args: Array[String]): Unit = {
    // Command line argument: path to HTML file
    if (args.length < 3) {
      throw new IllegalArgumentException("Expecting arguments: (1) input html file, (2) labels in csv, (3) output file")
    }
    val labels = Util.loadFile(args(1)).split(",").map(_.toInt)
    applyLabelsToPage(args(0), labels, args(2))
  }

  def applyLabelsToPage(filename: String, labels: Seq[Int], output: String) = {
    val featureExtractor = FeatureExtractor(
      DuplicateCountsExtractor
      + LeafBlockExtractor
      + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"), 1)
      + AncestorExtractor(NodeBlockExtractor, 2)
      + RootExtractor(NodeBlockExtractor)
      + TagExtractor(mode="leaf")//,
     // TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    )

    val source = Source.fromFile(filename).getLines.mkString
    val cdom = CDOM(source)
    Util.save(output, CleanTextOutput(cdom, labels))
  }
}
