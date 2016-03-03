package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor

/** Extractor that does and returns constant 1 */
object InterceptBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM): (Node) => Vector[Double] =
    (node: Node) => Vector(1)

  val labels: Vector[String] = Vector("intercept")

}