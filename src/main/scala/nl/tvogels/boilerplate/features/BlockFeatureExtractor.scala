package nl.tvogels.boilerplate.features

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.extractor.CombinedBlockExtractor

/** Trait for each block feature extractor.
  *
  * A `BlockFeatureExtractor` is initialized with a CDOM via `apply`,
  * and this returns a function that takes a leaf-node and returns a vector
  * of features (Double)
  */
trait BlockFeatureExtractor {

  def apply(cdom: CDOM): Node => Vector[Double]

  val labels: Vector[String]

  def +(other: BlockFeatureExtractor) =
    CombinedBlockExtractor(this,other)

}