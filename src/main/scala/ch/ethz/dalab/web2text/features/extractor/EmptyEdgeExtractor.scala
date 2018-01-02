package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.EdgeFeatureExtractor

/** Extractor that does and returns nothing */
object EmptyEdgeExtractor extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] =
    (node: Node, nodeb: Node) => Vector()

  val labels: Vector[String] = Vector()

}
