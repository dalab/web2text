package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.EdgeFeatureExtractor

/** Combine two or multiple extractors. Their outputs will be concatinated */
case class CombinedEdgeExtractor(exA: EdgeFeatureExtractor, exB: EdgeFeatureExtractor) extends EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] = {
    val (initExA, initExB) = (exA(cdom), exB(cdom))
    (nodeA: Node, nodeB: Node) => { initExA(nodeA,nodeB) ++ initExB(nodeA,nodeB) }
  }

  val labels = exA.labels ++ exB.labels

}

/** Factory for the `CombinedEdgeExtractor`.
  * Allows for concatinating many extractors simultaneously. */
object CombinedEdgeExtractor {
  def apply(exs: EdgeFeatureExtractor*): EdgeFeatureExtractor = {
    val zero: EdgeFeatureExtractor = EmptyEdgeExtractor
    exs.foldLeft(zero)((a,b) => CombinedEdgeExtractor(a,b))
  }
}
