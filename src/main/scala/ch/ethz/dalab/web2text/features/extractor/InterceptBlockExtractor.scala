package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.BlockFeatureExtractor

/** Extractor that does and returns constant 1 */
object InterceptBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM): (Node) => Vector[Double] =
    (node: Node) => Vector(1)

  val labels: Vector[String] = Vector("intercept")

}
