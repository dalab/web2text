package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor

/** Component for an extractor that combines the same features for a node, parent, grandparent, etc
  *
  * @param levels number of levels of the tree to include (leafs included)
  * @note If an ancestor of level n does not exist, fall back to ancester on level n-1
  */
case class AncestorExtractor(extractor: BlockFeatureExtractor, levels: Int) extends BlockFeatureExtractor {

  /** Compute the prefix for a certain level */
  def prefix(level: Int) = level match {
    case 0 => ""
    case _ => "g"*(level-1) + "p_"
  }

  /** Add a prefix to a list of strings */
  private def addPrefix(labels: Vector[String], prefix: String): Vector[String] =
    labels map { prefix + _}

  /** Feature name for the has_ field, for each level, although left out for the leaf itself */
  def fieldNameHas(level: Int) = level match {
    case 0 => ""
    case _ => "has_" + "g"*(level-1) + "p"
  }

  /** Return the labels of this extractor, built from the component's labels */
  val labels = (0 until levels).toVector flatMap {
    case 0 => extractor.labels
    case l => fieldNameHas(l) +: addPrefix(extractor.labels, prefix(l))
  }

  def apply(cdom: CDOM): (Node) => Vector[Double] = {
    val loadedExtractor = extractor(cdom)
    val zeroFeatures: Vector[Double] = extractor.labels map { x => 0.0 }
    (node: Node) => {
      var curNode = node

      var baseFeatures = loadedExtractor(curNode)

      val ancestorFeatures = (1 until levels).toVector flatMap { level => {
        curNode.parent match {
          case None => {
            0.0 +: zeroFeatures
          }
          case Some(node) => {
            curNode = node
            1.0 +: loadedExtractor(node)
          }
        }
      }}
      baseFeatures ++ ancestorFeatures
    }
  }

}