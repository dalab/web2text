package ch.ethz.dalab.web2text

import java.io.File
import scala.io.Source
import breeze.linalg.csvwrite
import ch.ethz.dalab.web2text.cdom.CDOM
import ch.ethz.dalab.web2text.features.extractor._
import ch.ethz.dalab.web2text.features.FeatureExtractor
import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.utilities.Settings

/**
 * This is the first step in classifying boilerplate content in a webpage.
 * This script takes two arguments:
 * (1) an input html file
 * (2) a basename for output features that should be passed into the neural net in Python
 */
object ExtractPageFeatures {

  def main(args: Array[String]): Unit = {
    // Command line argument: path to HTML file
    if (args.length < 2) {
      throw new IllegalArgumentException("Expecting arguments: (1) input html file, (2) output file base name")
    }
    val authorNames = Util.loadFile("/Users/cesc/Desktop/hypefactors/AuthorExtractor/src/main/resources/all_the_news_dataset/authors.csv", skipLines = 1)

    val filename = args(0)
    val articleId = filename.split("html/")(1).split(".html")(0)

    val lines = authorNames.split("\n")
    var author = "Unknown author"
    for (line <- lines){
      val elems = line.split(", ")
      if (elems.length==2){
        if (elems(0) == articleId)
        {
          author = elems(1)
        }
      }
    }

    System.out.println("Author: " + author)
    Settings.author = author

    extractPageFeatures(filename, args(1))
  }

  def extractPageFeatures(filename: String, outputBasename: String) = {

    val featureExtractor = FeatureExtractor(
      DuplicateCountsExtractor
      + LeafBlockExtractor
      + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"), 1)
      + AncestorExtractor(NodeBlockExtractor, 2)
      + RootExtractor(NodeBlockExtractor)
      + TagExtractor(mode="leaf")//,
      //TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    )

    val source = Util.loadFile(filename)
    val cdom = CDOM(source)
    val features = featureExtractor(cdom)

    for(feat <- features.blockFeatureLabels)
      {
        println("feat: " + feat)
      }

    csvwrite(new File(s"${outputBasename}_block_features.csv"), features.blockFeatures)
    cdom.saveHTML("/Users/cesc/Desktop/hypefactors/AuthorExtractor/public/dom/dom.html")
    //csvwrite(new File(s"${outputBasename}_edge_features.csv"), features.edgeFeatures)
  }
}
