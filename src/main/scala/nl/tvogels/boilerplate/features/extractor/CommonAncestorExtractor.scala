package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.{EdgeFeatureExtractor,BlockFeatureExtractor}

/** Combine two or multiple extractors. Their outputs will be concatinated */
case class CommonAncestorExtractor(ex: BlockFeatureExtractor) extends EdgeFeatureExtractor {

  private def firstCommonElement[X](a: Seq[X], b: Seq[X]): Option[X] = {
    if ((a lengthCompare b.length) > 0)
      b.find(x => a.contains(x))
    else
      a.find(x => b.contains(x))
  }

  def commonAncestor(nodeA: Node, nodeB: Node): Node =
    firstCommonElement(nodeA.ancestors, nodeB.ancestors).get


  def apply(cdom: CDOM): (Node,Node) => Vector[Double] = {
    val extractor = ex(cdom)
    (nodeA: Node, nodeB: Node) => {
      val ancestor = commonAncestor(nodeA, nodeB)
      extractor(ancestor)
    }
  }

  val labels = ex.labels.map(x => s"common_ancestor_$x")

}
