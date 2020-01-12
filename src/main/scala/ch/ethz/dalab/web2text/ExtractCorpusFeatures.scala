package ch.ethz.dalab.web2text

import ch.ethz.dalab.web2text.features.extractor.{AncestorExtractor, DuplicateCountsExtractor, NodeBlockExtractor, RootExtractor, TagExtractor}
import java.io.File
import scala.collection.mutable
import breeze.linalg.csvwrite
import ch.ethz.dalab.web2text.cdom.CDOM
import ch.ethz.dalab.web2text.features.extractor._
import ch.ethz.dalab.web2text.features.FeatureExtractor
import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.utilities.Settings

/**
 * This is the first step in classifying boilerplate content in a webpage.
 * This script takes two arguments:
 * (1) an folder containing multiple html files
 * (2) a basename for output features that should be passed into the neural net in Python
 */
object ExtractCorpusFeatures {

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      throw new IllegalArgumentException("Expecting arguments: (1) folder, (2) output file base name")
    }
    val folderName = args(0)
    val file = new File(folderName)
    val fileNameList = file.listFiles.filter(_.isFile)
      .filter(_.getName.endsWith("html"))
      .map(_.getPath).toList
    val authorNames = Util.loadFile(folderName + "../authors.csv", skipLines = 1)
    var authorNamesHM = mutable.Map[String, String]().withDefaultValue("Unkown Author")
    val lines = authorNames.split("\n")
    var i = 0
    for (line <- lines){
      i+=1
      val elems = line.split(";")
      if (elems.length==2){
        //print("i=", i, "elems(0)=", elems(0), "elems(1)", elems(1))
        authorNamesHM += (elems(0)->elems(1))
      }
    }
    i=0
    for (fileName <- fileNameList)
    {
          i+=1
          val articleId = fileName.split("html/")(1).split(".html")(0)
          System.out.println("Extracting features from file: " + fileName + ", articleId=" + articleId + " [" + i + " / " + fileNameList.size + "]")
          val lines = authorNames.split("\n")
          var author = authorNamesHM(articleId)
          System.out.println("True Label Author: " + author)
          Settings.author = author
          extractPageFeatures(fileName, args(1), articleId)
    }
  }
  def extractPageFeatures(filename: String, outputBasename: String, articleId: String) = {

    // Open the author file and read the real author

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

    csvwrite(new File(s"${outputBasename}${articleId}.csv"), features.blockFeatures)
    cdom.saveHTML("/Users/cesc/Desktop/hypefactors/AuthorExtractor/public/dom/dom.html")
    //csvwrite(new File(s"${outputBasename}_edge_features.csv"), features.edgeFeatures)
  }
}
