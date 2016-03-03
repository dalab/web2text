package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.EdgeFeatureExtractor

/** Extractor that does and returns constant 1 */
object InterceptEdgeExtractor extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] =
    (node: Node, nodeb: Node) => Vector(1)

  val labels: Vector[String] = Vector("intercept")

}