package nl.tvogels.boilerplate

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.utilities.Util._
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.cleaneval.{CleanEval,Page}
import nl.tvogels.boilerplate.cdom.{CDOM,DOM}
import org.jsoup.Jsoup
import nl.tvogels.boilerplate.features.extractor._
import nl.tvogels.boilerplate.classification.{PerformanceStatistics,ChainCRF,ChainCRFModel}
import nl.tvogels.boilerplate.features.{BlockFeatureExtractor,FeatureExtractor}
import nl.tvogels.boilerplate.database.Database
import nl.tvogels.boilerplate.features.PageFeatures
import com.mongodb.casbah.Imports._
object Main {

  def main(args: Array[String]): Unit = {
    testNewChainCRF
  }

  def testNewChainCRF = {
    scala.util.Random.setSeed(14101992)
    println("Load dataset")
    val fe = FeatureExtractor(
      AncestorExtractor(BasicBlockExtractor,levels=2),
      InterceptEdgeExtractor
    )
    val data = time{CleanEval.dataset(fe)}
    println("Dataset loaded")
    val splits = data.randomSplit(0.5,0.5);
    val (train,test) = (splits(0),splits(1))
    val classifier = ChainCRF(
      blockFeatureLabels=fe.blockExtractor.labels,
      edgeFeatureLabels=fe.edgeExtractor.labels,
      lambda = 10,
      debug=false
    )
    classifier.train(train,test)
    classifier.saveWeights("output/weights.txt")
    classifier.saveWeightsHuman("output/weights-human.txt")
    println(s"Training statistics: ${classifier.performanceStatistics(train)}")
    println(s"Test statistics:     ${classifier.performanceStatistics(test)}")

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

  // def addOursToMongo = {

  //   val labelName = "ours-v1"

  //   val local = new Database
  //   scala.util.Random.setSeed(14101992)
  //   println("Load dataset")
  //   val data = time{CleanEval.dataset(
  //     FeatureExtractor(AncestorExtractor(BasicBlockExtractor,levels=2),EmptyEdgeExtractor)
  //   )}
  //   println("Dataset loaded")
  //   val splits = data.randomSplit(0.5,0.5);
  //   val (train,test) = (splits(0),splits(1))
  //   val classifier = ChainCRF(lambda = 10,debug=false,disablePairwise=false)
  //   classifier.train(train)
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

  // def testChainCRF = {
  //   scala.util.Random.setSeed(14101992)
  //   println("Load dataset")
  //   val data = time{CleanEval.dataset(
  //     FeatureExtractor(AncestorExtractor(BasicBlockExtractor,levels=2),EmptyEdgeExtractor)
  //   )}
  //   println("Dataset loaded")
  //   val splits = data.randomSplit(0.5,0.5);
  //   val (train,test) = (splits(0),splits(1))
  //   val classifier = ChainCRF(lambda = 10,debug=false,disablePairwise=false)
  //   classifier.train(train,test)
  //   classifier.saveWeights("output/weights.txt")
  //   println(s"Training statistics: ${classifier.performanceStatistics(train)}")
  //   println(s"Test statistics:     ${classifier.performanceStatistics(test)}")

  // }

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