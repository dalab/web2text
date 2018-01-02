package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.{EdgeFeatureExtractor,BlockFeatureExtractor}

object TreeDistanceExtractor extends EdgeFeatureExtractor {

  private def firstCommonElement[X](a: Seq[X], b: Seq[X]): Option[X] = {
    if ((a lengthCompare b.length) > 0)
      b.find(x => a.contains(x))
    else
      a.find(x => b.contains(x))
  }



  def apply(cdom: CDOM): (Node,Node) => Vector[Double] = {
    (nodeA: Node, nodeB: Node) => {
      val aAncestors = nodeA.ancestors
      val bAncestors = nodeB.ancestors
      val ancestor = firstCommonElement(aAncestors, bAncestors).get
      val depth = aAncestors.indexOf(ancestor) + bAncestors.indexOf(ancestor) + 2
      implicit def b2d(b: Boolean): Double = if (b) 1.0 else -1.0
      Vector(depth==2, depth==3, depth==4, depth>4)
    }
  }

  val labels = Vector("tree_distance_2","tree_distance_3","tree_distance_4","tree_distance_more")

}
