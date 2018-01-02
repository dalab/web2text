package ch.ethz.dalab.web2text.features.extractor

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import ch.ethz.dalab.web2text.features.BlockFeatureExtractor

/** Combine two or multiple extractors. Their outputs will be concatinated */
case class CombinedBlockExtractor(exA: BlockFeatureExtractor, exB: BlockFeatureExtractor) extends BlockFeatureExtractor {

  def apply(cdom: CDOM): (Node) => Vector[Double] = {
    val (initExA, initExB) = (exA(cdom), exB(cdom))
    (node: Node) => { initExA(node) ++ initExB(node) }
  }

  val labels = exA.labels ++ exB.labels

}

/** Factory for the `CombinedBlockExtractor`.
  * Allows for concatinating many extractors simultaneously. */
object CombinedBlockExtractor {
  def apply(exs: BlockFeatureExtractor*): BlockFeatureExtractor = {
    val zero: BlockFeatureExtractor = EmptyBlockExtractor
    exs.foldLeft(zero)((a,b) => CombinedBlockExtractor(a,b))
  }
}
