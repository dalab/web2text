package nl.tvogels.boilerplate.classification

import nl.tvogels.boilerplate.features.PageFeatures

trait Classifier {

  def train(data: Vector[(PageFeatures,Vector[Int])]): Unit

  def saveWeights(filename: String): Unit

  def loadWeights(filename: String): Unit

  def predict(features: PageFeatures): Vector[Int]

  def predict(features: Vector[PageFeatures]): Vector[Vector[Int]] =
    features.map(predict)

  def performanceStatistics(data: Vector[(PageFeatures,Vector[Int])]) = {
    val results: Vector[(Int,Int)] = data flatMap {
      case (features, label) => (predict(features) zip label)
    }
    PerformanceStatistics.fromPairs(results)
  }

}