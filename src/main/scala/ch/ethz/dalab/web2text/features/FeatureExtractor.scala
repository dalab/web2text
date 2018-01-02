package ch.ethz.dalab.web2text.features

import ch.ethz.dalab.web2text.cdom.{Node,CDOM}
import breeze.{linalg => la}

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

    val nBlocks = cdom.leaves.length
    val edgeFeatureLength  = edgeExtractor.labels.length
    val blockFeatureLength = blockExtractor.labels.length

    val blockFeatures = (cdom.leaves flatMap { blockEx(_) }).toArray
    val pairs         = (cdom.leaves zip cdom.leaves.tail)
    val edgeFeatures  = (pairs flatMap { case (a,b) => edgeEx(a,b) }).toArray

    PageFeatures(
      blockFeatures = la.DenseVector(blockFeatures)
                        .toDenseMatrix
                        .reshape(blockFeatureLength,nBlocks),
      blockFeatureLabels = blockExtractor.labels,
      edgeFeatures = la.DenseVector(edgeFeatures)
                        .toDenseMatrix
                        .reshape(edgeFeatureLength,nBlocks-1),
      edgeFeatureLabels = edgeExtractor.labels
    )
  }

}
