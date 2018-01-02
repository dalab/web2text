package ch.ethz.dalab.web2text.features

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.extractor.CombinedEdgeExtractor

/** Trait for each edge feature extractor.
  *
  * A `EdgeFeatureExtractor` is initialized with a CDOM via `apply`,
  * and this returns a function that takes two leaf-nodes and returns a vector
  * of features (Double)
  */
trait EdgeFeatureExtractor {

  def apply(cdom: CDOM): (Node,Node) => Vector[Double]

  /** The labels */
  val labels: Vector[String]

  def +(other: EdgeFeatureExtractor) =
    CombinedEdgeExtractor(this,other)
}
