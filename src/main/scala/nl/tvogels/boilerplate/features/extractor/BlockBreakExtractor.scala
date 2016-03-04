package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.EdgeFeatureExtractor

/** Extractor has one variable: whether there is a block break between nodes */
object BlockBreakExtractor extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] =
    (node: Node, nodeb: Node) => {
      Vector(if (node.properties.blockBreakAfter || nodeb.properties.blockBreakBefore) 1.0 else 0.0)
    }

  val labels: Vector[String] = Vector("block_break")

}