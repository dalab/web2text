package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.BlockFeatureExtractor

/** Component for an extractor that combines the same features for a node, parent, grandparent, etc
  *
  * @param levels number of levels of the tree to include (leafs included)
  * @note If an ancestor of level n does not exist, fall back to ancester on level n-1
  */
case class AncestorExtractor(extractor: BlockFeatureExtractor, level: Int) extends BlockFeatureExtractor {

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
  val labels = fieldNameHas(level) +: addPrefix(extractor.labels, prefix(level))

  def apply(cdom: CDOM): (Node) => Vector[Double] = {
    val loadedExtractor = extractor(cdom)
    val zeroFeatures: Vector[Double] = extractor.labels map { x => 0.0 }
    (node: Node) => {
      if (node.ancestors.length < level)
        0.0 +: zeroFeatures
      else
        1.0 +: loadedExtractor(node.ancestors(level-1))
    }
  }

}
