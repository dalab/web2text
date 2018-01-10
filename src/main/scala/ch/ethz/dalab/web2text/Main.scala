package ch.ethz.dalab.web2text

import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.utilities.Util._
import ch.ethz.dalab.web2text.cleaneval.{CleanEval,Page}
import ch.ethz.dalab.web2text.cdom.{CDOM,DOM}
import org.jsoup.Jsoup
import ch.ethz.dalab.web2text.features.extractor._
import ch.ethz.dalab.web2text.classification.{PerformanceStatistics}
import ch.ethz.dalab.web2text.features.{BlockFeatureExtractor,FeatureExtractor}
import ch.ethz.dalab.web2text.features.PageFeatures
import com.mongodb.casbah.Imports._
import java.io.File;
import ch.ethz.dalab.web2text.output.CsvDatasetWriter
import ch.ethz.dalab.web2text.utilities.Warc
import ch.ethz.dalab.web2text.output.CleanTextOutput
import scala.util.{Try,Success,Failure}

object Main {

  def main(args: Array[String]): Unit = {
    // testWarcLoad
    // exportFeaturesTest
    exportFeaturesTest
  }

  def exportFeaturesTest = {
    val fe = FeatureExtractor(
      DuplicateCountsExtractor
      + LeafBlockExtractor
      + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"), 1)
      + AncestorExtractor(NodeBlockExtractor, 2)
      + RootExtractor(NodeBlockExtractor)
      + TagExtractor(mode="leaf"),
      TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    )
    val data = Util.time{ CleanEval.dataset(fe) }
    CsvDatasetWriter.write(data, "/Users/thijs/Desktop/export")
    println("# Block features")
    fe.blockExtractor.labels.foreach(println)
    println("# Edge features")
    fe.edgeExtractor.labels.foreach(println)
  }

  def testCommonAncestorExtractor = {
    val ex = CommonAncestorExtractor(LeafBlockExtractor)
    val cdom = CDOM.fromHTML("<body><h1>Header</h1><p>Paragraph with an <i>Italic</i> section.</p></body>");
    ex(cdom)(cdom.leaves(2),cdom.leaves(1))
  }

  def evaluateOtherMethods = {
    val dir = "other_frameworks/output/"
    val cleaners = Iterable(
      "victor"            -> ((id: Int) => s"$dir/victor/$id-aligned.txt"),
      "bte"               -> ((id: Int) => s"$dir/bte/$id-aligned.txt"),
      "article-extractor" -> ((id: Int) => s"$dir/article-extractor/$id-aligned.txt"),
      "default-extractor" -> ((id: Int) => s"$dir/default-extractor/$id-aligned.txt"),
      "largest-content"   -> ((id: Int) => s"$dir/largestcontent-extractor/$id-aligned.txt"),
      "unfluff"           -> ((id: Int) => s"$dir/unfluff/$id-aligned.txt")
    )

    for ((label, filenameGen) <- cleaners) {
      val title = s"#### Evaluating ‘${label.capitalize}’ "
      println(s"\n$title${"#"*(82-title.length)}\n")
      Util.time {
        val eval = CleanEval.evaluateCleaner(filenameGen)
        println(s"$eval")
      }
    }
  }

  def alignCleanEvalData = {
    val projectPath = "/Users/thijs/dev/boilerplate"
    val dir = s"$projectPath/src/main/resources/cleaneval/aligned"
    CleanEval.generateAlignedFiles(dir)
  }
}
