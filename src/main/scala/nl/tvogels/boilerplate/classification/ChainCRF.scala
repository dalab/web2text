package nl.tvogels.boilerplate.classification

import nl.tvogels.boilerplate.features.PageFeatures

case class ChainCRF() extends Classifier {

  def train(data: Vector[(PageFeatures,Vector[Int])]): Unit = {

  }

  def saveWeights(filename: String): Unit = {

  }

  def loadWeights(filename: String): Unit = {

  }

  def predict(features: PageFeatures): Vector[Int] = {
    null
  }


}