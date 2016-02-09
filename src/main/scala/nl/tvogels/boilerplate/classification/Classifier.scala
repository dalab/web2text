package nl.tvogels.boilerplate.classification

import nl.tvogels.boilerplate.features.PageFeatures

trait Classifier {

  def train(data: Seq[(PageFeatures,Seq[Int])]): Unit

  def saveWeights(filename: String): Unit

  def loadWeights(filename: String): Unit

  def predict(features: PageFeatures): Vector[Int]

  def predict(features: Seq[PageFeatures]): Seq[Vector[Int]] =
    features.map(predict)

  def performanceStatistics(data: Seq[(PageFeatures,Vector[Int])]) = {
    val results: Seq[(Int,Int)] = data flatMap {
      case (features, label) => (predict(features) zip label)
    }
    PerformanceStatistics.fromPairs(results)
  }

}