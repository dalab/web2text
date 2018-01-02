package ch.ethz.dalab.web2text.features

import breeze.{linalg => la}

/** Container for page features
 *
 * @param blockFeatures vector of features related to blocks
 * @param edgeFeatures vector of features related to neighboring pairs of blocks
 */
case class PageFeatures(
    blockFeatures: la.DenseMatrix[Double],
    blockFeatureLabels: Vector[String],
    edgeFeatures: la.DenseMatrix[Double],
    edgeFeatureLabels: Vector[String]
) {

  assert(
    blockFeatures.cols == edgeFeatures.cols + 1,
    "There should be one more block feature than edge features"
  )

  /** Number of blocks */
  val nBlocks = blockFeatures.cols

  override def toString = List(
    "\n++++++++++++++++++++",
    "++ Block features ++",
    "++++++++++++++++++++\n",
    (blockFeatureLabels.toIterator zip blockFeatures.toString.lines)
      map { case (lab, feat) => lab + "  " + feat} mkString "\n",
    "\n++++++++++++++++++++",
    "++ Edge features  ++",
    "++++++++++++++++++++\n",
    (edgeFeatureLabels.toIterator zip edgeFeatures.toString.lines)
      map { case (lab, feat) => lab + "  " + feat} mkString "\n"
  ) mkString "\n"

}
