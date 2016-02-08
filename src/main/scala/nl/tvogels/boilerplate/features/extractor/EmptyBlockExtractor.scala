package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor

/** Extractor that does and returns nothing */
object EmptyBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM): (Node) => Vector[Double] =
    (node: Node) => Vector()

  val labels: Vector[String] = Vector()

}