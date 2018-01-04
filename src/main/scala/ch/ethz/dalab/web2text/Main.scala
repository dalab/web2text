package ch.ethz.dalab.web2text

import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.utilities.Util._
import ch.ethz.dalab.web2text.alignment.Alignment
import ch.ethz.dalab.web2text.cleaneval.{CleanEval,Page}
import ch.ethz.dalab.web2text.cdom.{CDOM,DOM}
import org.jsoup.Jsoup
import ch.ethz.dalab.web2text.features.extractor._
import ch.ethz.dalab.web2text.classification.{PerformanceStatistics,ChainCRF,ChainCRFModel}
import ch.ethz.dalab.web2text.features.{BlockFeatureExtractor,FeatureExtractor}
import ch.ethz.dalab.web2text.database.Database
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
    evaluateOtherMethods
    // cleanWarcFile("/Users/thijs/Desktop/0000tw-00.warc.gz","/Users/thijs/Desktop/0000tw-00.clean.warc.gz")
  }

  def testWarcLoad = {
    import java.io.{File, FileInputStream, DataInputStream, BufferedInputStream, BufferedReader, InputStreamReader}
    import java.util.zip.GZIPInputStream

    val f   = new File("/Users/thijs/Desktop/0000tw-00.warc")
    val is  = new FileInputStream(f)
    val zip = new GZIPInputStream(is)
    val decoder = new InputStreamReader(zip, "utf8")
    val reader = new BufferedReader(decoder)
    (1 to 100).foreach(i => println(reader.readLine()))
  }

  def cleanWarcFile(file: String, outfile: String) = {
    println("Loading model ...")
    val fe = FeatureExtractor(
      DuplicateCountsExtractor
      + LeafBlockExtractor
      + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"),1)
      + AncestorExtractor(NodeBlockExtractor,2)
      + RootExtractor(NodeBlockExtractor)
      + TagExtractor(mode="leaf"),
      TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    )
    val classifier = ChainCRF(
      blockFeatureLabels  = fe.blockExtractor.labels,
      edgeFeatureLabels   = fe.edgeExtractor.labels,
      lambda              = 0.00562,
      debug               = false
    )
    classifier.loadWeights("output/trained-weights.txt")
    println("Done.")


    println("Reading WARC input file ...")
    val it       = Warc.iteratorForFile(file)
    print("Done.")
    println("Extracting DOM ...")

    val cdom  = it.toList.flatten
                .map(x => Warc.headersAndContent(x))
                .map(x => x._2)
                .filter(x => x.trim() != "")
                .map(x => Try(CDOM.fromHTML(x)))

    println("Done.")
    println("Extracting features and predictng ...")
    val cleaned = cdom.map {
      case Success(cdom) => {
        val features = fe(cdom)
        val labels = classifier.predict(features)
        CleanTextOutput(cdom, labels)
      }
      case Failure(cdom) => ""
    }
    println("Done.")
    print(cleaned(0))
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

  def trainModel = {
    val labelName = "ours-with-tree-distance"

    scala.util.Random.setSeed(14101992)

    val fe = FeatureExtractor(
      DuplicateCountsExtractor
      + LeafBlockExtractor
      + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"),1)
      + AncestorExtractor(NodeBlockExtractor,2)
      + RootExtractor(NodeBlockExtractor)
      + TagExtractor(mode="leaf"),
      TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    )

    println("Loading datasets")
    val train = CleanEval.trainingDataset(fe)
    val test  = CleanEval.testDataset(fe)
    println("Datasets (trian+test) loaded")

    val classifier = ChainCRF(
      blockFeatureLabels  = fe.blockExtractor.labels,
      edgeFeatureLabels   = fe.edgeExtractor.labels,
      lambda              = 0.00562,
      debug               = false
    )
    classifier.train(train,test)
    classifier.saveWeights("output/trained-weights.txt")
  }

  def testChainCRF(addToMongo: Boolean = true) = {
    val labelName = "ours-with-tree-distance"

    scala.util.Random.setSeed(14101992)
    // val fe = FeatureExtractor(
    //   AncestorExtractor(BasicBlockExtractor+TagExtractor, levels=3)+RootExtractor(BasicBlockExtractor),
    //   InterceptEdgeExtractor+TreeDistanceExtractor+BlockBreakExtractor+CommonAncestorExtractor(BasicBlockExtractor)
    // )
    val fe = FeatureExtractor(
      DuplicateCountsExtractor
      + LeafBlockExtractor
      + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"),1)
      + AncestorExtractor(NodeBlockExtractor,2)
      + RootExtractor(NodeBlockExtractor)
      + TagExtractor(mode="leaf"),
      TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    )
    // TreeDistanceExtractor + BlockBreakExtractor + CommonAncestorExtractor(NodeBlockExtractor)
    println("Loading datasets")
    val train = CleanEval.trainingDataset(fe)
    val test  = CleanEval.testDataset(fe)
    println("Datasets (trian+test) loaded")

    for (power <- -4.0 to 3.0 by 0.25; lambda = math.pow(10,power)) {
      val classifier = ChainCRF(
        blockFeatureLabels  = fe.blockExtractor.labels,
        edgeFeatureLabels   = fe.edgeExtractor.labels,
        lambda              = lambda,
        debug               = false
      )
      classifier.train(train,test)
      classifier.saveWeights("output/weights.txt")
      classifier.saveWeightsHuman("output/weights-human.txt")

      println(f"Lambda: $lambda%1.5f")
      println(s"Training statistics: ${classifier.performanceStatistics(train)}")
      println(s"Test statistics:     ${classifier.performanceStatistics(test)}\n")
    }

    // if (addToMongo) {
    //   println("Adding to MongoDB")
    //   val local = new Database
    //   data.iterator zip CleanEval.iterator foreach {
    //     case ((features,_), p) => {
    //       val prediction = classifier.predict(features)
    //       local.insertLabels(
    //         docId         = p.docId,
    //         dataset       = "cleaneval",
    //         labelName     = labelName,
    //         labels        = prediction,
    //         userGenerated = false,
    //         metadata      = Map()
    //       )
    //     }
    //   }
    // }

  }

  def testFeatureFn = {
    import breeze.linalg.DenseVector
    val nBlocks = 3
    val blockFeatureLength = 5
    val edgeFeatureLength = 5
    val blockFeatures = (0 until blockFeatureLength).map(_.toDouble).toArray
    val edgeFeatures  = (0 until edgeFeatureLength).map(_.toDouble).toArray
    val feat = PageFeatures(
      DenseVector(((0 until nBlocks) flatMap (x=>blockFeatures)).toArray)
        .toDenseMatrix.reshape(blockFeatureLength, nBlocks) ,
      ((0 until blockFeatureLength) map (x=>"a")).toVector,
      DenseVector(((0 until (nBlocks-1)) flatMap (x=>edgeFeatures)).toArray)
        .toDenseMatrix.reshape(edgeFeatureLength, nBlocks-1) ,
      ((0 until edgeFeatureLength) map (x=>"ea")).toVector
    )
    val labels = DenseVector.ones[Double](nBlocks)
    println(feat)
  }

  def convertAnnotationsToNewDBFormat = {
    val remote = (new Database(port=27018)).db
    val local = new Database
    local.db("pages").find(MongoDBObject("labels.user-dqLguDwKvoHxXEAKk"->MongoDBObject("$exists"->true))) foreach { p =>
      val labels = p.get("labels").asInstanceOf[com.mongodb.BasicDBObject]
      val thijsl = labels.get("user-dqLguDwKvoHxXEAKk").asInstanceOf[com.mongodb.BasicDBList]
                   .toList.map(_.asInstanceOf[Int])
      local.insertLabels(
        docId=p.get("doc_id").asInstanceOf[String],
        dataset="cleaneval",
        labelName="user-dqLguDwKvoHxXEAKk",
        labels=thijsl,
        userGenerated=true,
        metadata=Map("finished"->java.lang.Boolean.TRUE)
      )
    }
  }

  def addCleanEvalEvaluationsToMongo = {
    val db = new Database
    val dir = "other_frameworks/output/"
    val cleaners = Iterable(
      "victor"            -> ((id: Int) => s"$dir/victor/$id-aligned.txt"),
      "bte"               -> ((id: Int) => s"$dir/bte/$id-aligned.txt"),
      "article-extractor" -> ((id: Int) => s"$dir/article-extractor/$id-aligned.txt"),
      "default-extractor" -> ((id: Int) => s"$dir/default-extractor/$id-aligned.txt"),
      "largest-content"   -> ((id: Int) => s"$dir/largestcontent-extractor/$id-aligned.txt"),
      "unfluff"           -> ((id: Int) => s"$dir/unfluff/$id-aligned.txt")
    )

    for ((id, fileFunc) <- cleaners;
         p <- CleanEval.iterator) {

      println(s"Working on page ${p.docId} for cleaner ‘${id.capitalize}’ ...")
      val filename = fileFunc(p.id)
      if (Util.fileExists(filename)) {
        val aligned = Util.loadFile(fileFunc(p.id))
        val labels = Alignment.extractLabels(CDOM(p.source), aligned)
        db.insertLabels(
          docId         = p.docId,
          dataset       = "cleaneval",
          labelName     = id,
          labels        = labels,
          userGenerated = false,
          metadata      = Map("aligned"->aligned)
        )
      }
    }
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
