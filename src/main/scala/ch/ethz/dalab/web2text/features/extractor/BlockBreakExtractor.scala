package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.EdgeFeatureExtractor

/** Extractor has one variable: whether there is a block break between nodes */
object BlockBreakExtractor extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] =
    (node: Node, nodeb: Node) => {
      Vector(if (node.properties.blockBreakAfter || nodeb.properties.blockBreakBefore) 1.0 else 0.0)
    }

  val labels: Vector[String] = Vector("block_break")

}
