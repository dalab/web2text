package nl.tvogels.boilerplate.features

import nl.tvogels.boilerplate.cdom.{Node,CDOM}

/** FeatureExtractor, consisting of a BlockFeatureExtractor and an EdgeFeatureExtractor
  *
  */
case class FeatureExtractor (
  blockExtractor: BlockFeatureExtractor,
  edgeExtractor:  EdgeFeatureExtractor
) {

  def apply(cdom: CDOM): PageFeatures = {
    // Initialize the extractors with the CDOM information
    val (blockEx, edgeEx) = (blockExtractor(cdom), edgeExtractor(cdom))

    PageFeatures(
      blockFeatures = cdom.leaves map { blockEx(_) },
      blockFeatureLabels = blockExtractor.labels,
      edgeFeatures = (cdom.leaves zip cdom.leaves.tail) map { case (a,b) => edgeEx(a,b) },
      edgeFeatureLabels = edgeExtractor.labels
    )
  }

}