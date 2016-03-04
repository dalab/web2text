package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.{EdgeFeatureExtractor,BlockFeatureExtractor}

/** Combine two or multiple extractors. Their outputs will be concatinated */
case class CommonAncestorExtractor(ex: BlockFeatureExtractor) extends EdgeFeatureExtractor {

  def commonAncestor(nodeA: Node, nodeB: Node): Node = {
    val aAncestors = Vector(nodeA, nodeA.parent.get, nodeA.parent.get.parent.get)
    println(aAncestors mkString "\n\n")
    null
  }

  def apply(cdom: CDOM): (Node,Node) => Vector[Double] = {
    (nodeA: Node, nodeB: Node) => {
      val ancestor = commonAncestor(nodeA, nodeB)
      Vector()
    }
  }

  val labels = Vector()

}
