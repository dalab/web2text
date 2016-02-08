package nl.tvogels.boilerplate.features

/** Container for page features
 *
 * @param blockFeatures vector of features related to blocks
 * @param edgeFeatures vector of features related to neighboring pairs of blocks
 */
case class PageFeatures(blockFeatures: Vector[Vector[Double]],  blockFeatureLabels: Vector[String], edgeFeatures: Vector[Vector[Double]], edgeFeatureLabels: Vector[String]) {

  assert(
    blockFeatures.length == edgeFeatures.length + 1,
    "There should be one more block feature than edge features"
  )

  /** Number of blocks */
  val nBlocks = blockFeatures.length

  override def toString = List(
    "\n++++++++++++++++++++",
    "++ Block features ++",
    "++++++++++++++++++++\n",
    (blockFeatureLabels mkString "\t"),
    (blockFeatures.map(_ mkString "\t") mkString "\n").trim,
    "\n++++++++++++++++++++",
    "++ Edge features  ++",
    "++++++++++++++++++++\n",
    (edgeFeatureLabels mkString "\t"),
    (edgeFeatures.map(_ mkString "\t") mkString "\n").trim
  ) mkString "\n"

}