package nl.tvogels.boilerplate.classification

import nl.tvogels.boilerplate.features.PageFeatures

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.{SparkConf,SparkContext}
import breeze.linalg
import ch.ethz.dalab.dissolve.regression.LabeledObject
import ch.ethz.dalab.dissolve.models.LinearChainCRF
import ch.ethz.dalab.dissolve.optimization.{LocalSSGD,DistBCFW}
import ch.ethz.dalab.dissolve.optimization.SSVMClassifier

case class ChainCRF(
  blockFeatureLabels: Vector[String],
  edgeFeatureLabels: Vector[String],
  lambda: Double = 0.00001,
  debug: Boolean = false,
  debugMultiplier: Int = 0
) extends Classifier {

  /** Weights, containing a trained model */
  // var weights: linalg.Vector[Double] = null

  // Set up the logger
  Logger.getLogger("ch.ethz.dalab.dissolve.optimization.LAdap$").setLevel(Level.OFF)
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)


  def transformY(labels: Seq[Int]): linalg.Vector[Double] =
    linalg.DenseVector(labels.map(_.toDouble).toArray)


  /** Transform one data item from input format to a LabeledObject for Dissolve */
  def transformData(data: (PageFeatures,Seq[Int])):
    LabeledObject[PageFeatures, linalg.Vector[Double]] =
      new LabeledObject(transformY(data._2),data._1)


  /** The Spark context to be used, lazy so that it will only be loaded when needed */
  lazy val spark: SparkContext = {
    val conf = new SparkConf().setAppName("ChainCRF Classifier").setMaster("local")
    val sc = new SparkContext(conf)
    sc.setCheckpointDir("logs/checkpoint-files")
    sc
  }

  /** Model configuration */
  val modelConfig = new ChainCRFModel(
    blockFeatureLabels=blockFeatureLabels,
    edgeFeatureLabels=edgeFeatureLabels
  )

  /** Classifier */
  val classifier = new SSVMClassifier(modelConfig)

  /** Train the model without test dataset */
  def train(data: Seq[(PageFeatures,Seq[Int])]): Unit =
    train(data, testData=Vector())

  /** Train the model */
  def train(data: Seq[(PageFeatures,Seq[Int])], testData: Seq[(PageFeatures,Seq[Int])]): Unit = {

    // Transform the data to the right format
    val d = data.map(transformData)
    val t = testData.map(transformData)
    // val train_data = spark.parallelize(d)
    // val test_data = spark.parallelize(t)

    // Train the model

    val solver = new LocalSSGD(
      modelConfig,
      debug=debug,
      // debugMultiplier=debugMultiplier,
      lambda=lambda
      // roundLimit=2000
    )

    // Save the model
    classifier.train(d, t, solver)
  }

  /** Store the model weights to file */
  def saveWeights(filename: String) = classifier.saveWeights(filename)

  /** Load the model weights from a file */
  def loadWeights(filename: String) = classifier.loadWeights(filename)

  def predict(features: PageFeatures): Vector[Int] = {
    classifier.predict(features).toArray.map(_.toInt).toVector
  }


}