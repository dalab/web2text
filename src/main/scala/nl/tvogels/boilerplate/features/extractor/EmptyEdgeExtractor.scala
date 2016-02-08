package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.EdgeFeatureExtractor

/** Extractor that does and returns nothing */
object EmptyEdgeExtractor extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] =
    (node: Node, nodeb: Node) => Vector()

  val labels: Vector[String] = Vector()

}