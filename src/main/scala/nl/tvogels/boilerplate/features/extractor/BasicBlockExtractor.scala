package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor

object BasicBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM) = (node: Node) => Vector(
    node.properties.nCharacters,
    node.properties.nStopwords
  )

  val labels = Vector("n_characters","n_stopwords")


}