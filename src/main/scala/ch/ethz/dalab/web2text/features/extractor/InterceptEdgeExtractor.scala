package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.EdgeFeatureExtractor

/** Extractor that does and returns constant 1 */
object InterceptEdgeExtractor extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] =
    (node: Node, nodeb: Node) => Vector(1)

  val labels: Vector[String] = Vector("intercept")

}
