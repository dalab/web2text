package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor

object BasicBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM) = (node: Node) => Vector(
    node.properties.nCharsInLink.toDouble / node.properties.nCharacters,
    node.properties.nStopwords.toDouble / node.properties.nWords
  )

  val labels = Vector("link_density","stopword_ratio")


}