package ch.ethz.dalab.web2text.classification

import ch.ethz.dalab.dissolve.optimization.DissolveFunctions
import ch.ethz.dalab.web2text.features.{PageFeatures,FeatureExtractor}
import breeze.{linalg => la}

class ChainCRFModel(
 blockFeatureLabels: Vector[String],
 edgeFeatureLabels: Vector[String],
 normalizedHammingLoss: Boolean = true
) extends DissolveFunctions[PageFeatures, la.Vector[Double]] {

  val blockFeatureLength = blockFeatureLabels.length
  val edgeFeatureLength  = edgeFeatureLabels.length

  val weightNames: Vector[String] = {
    val bNames = blockFeatureLabels
    val eNames = edgeFeatureLabels
    bNames.map(n => s"0_"+n) ++ bNames.map(n => s"1_"+n) ++
    eNames.map(n => s"00_"+n) ++ eNames.map(n => s"01_"+n) ++
    eNames.map(n => s"10_"+n) ++ eNames.map(n => s"11_"+n)
  }

  /** Ordering of edge pairs (00 -> 0, 01 -> 1, 10 -> 3, 11 -> 4) */
  private def edgeCombiIndex(labelA: Int, labelB: Int) =
    2 * labelA + labelB

  def featureFn(x: PageFeatures, y: la.Vector[Double]): la.Vector[Double] = {
    // Construct a vector with the features
    // 1) sum of block features for blocks with label 0
    // 2) sum of block features for blocks with label 1
    // 3a) sum of edge features for edges with labels 00
    // 3b) sum of edge features for edges with labels 01
    // 3c) sum of edge features for edges with labels 10
    // 3d) sum of edge features for edges with labels 11
    val vecLength = (blockFeatureLength * 2) + (edgeFeatureLength * 4)
    val nBlocks   = x.blockFeatures.cols

    val phi = la.DenseVector.zeros[Double](vecLength)

    // Block features
    for (i <- 0 until nBlocks) {
      val label = y(i).toInt
      val p = label*blockFeatureLength
      phi(p until (p+blockFeatureLength)) :+= x.blockFeatures(::, i)
    }

    // Edge featues
    val offset = blockFeatureLength * 2
    for (i <- 0 until (nBlocks-1)) {
      val labelA = y(i).toInt
      val labelB = y(i+1).toInt
      val p = offset + edgeCombiIndex(labelA, labelB)*edgeFeatureLength
      phi(p until (p+edgeFeatureLength)) :+= x.edgeFeatures(::, i)
    }

    phi
  }

  private case class Weights (
    unary:    la.DenseMatrix[Double],
    pairwise: la.DenseMatrix[Double]
  )

  private object Weights {
    def fromVector(v: la.Vector[Double]): Weights = Weights(
      unary = v(0 until 2 * blockFeatureLength)
        .toDenseVector
        .toDenseMatrix
        .reshape(blockFeatureLength, 2),
      pairwise = v(2 * blockFeatureLength until v.size)
        .toDenseVector
        .toDenseMatrix
        .reshape(edgeFeatureLength,4)
    )
  }

  /**
   * The Maximization Oracle
   *
   * Performs loss-augmented decoding on a given example xi and label yi
   * using model.getWeights() as parameter. The loss is normalized Hamming loss.
   *
   * If yi is not given, then standard prediction is done (i.e. MAP decoding),
   * without any loss term.
   */
  def oracleFnWithDecode(
      weightVec: la.Vector[Double],
      xi: PageFeatures,
      yi: la.Vector[Double],
      decodeFn: (la.DenseMatrix[Double], la.DenseMatrix[Double]) => la.Vector[Double]
  ): la.Vector[Double] = {

    val weights = Weights.fromVector(weightVec)

    // Construct 2 x (num blocks) matrix with cost of labels 0 or 1
    //     and a 4 x (num blocks - 1) matrix with cost 00, 01, 10 or  11
    val thetaUnary    = weights.unary.t * xi.blockFeatures
    val thetaPairwise = xi.edgeFeatures.rows match {
      case 0 => la.DenseMatrix.zeros[Double](4, xi.edgeFeatures.cols)
      case _ => weights.pairwise.t * xi.edgeFeatures
    }

    // Add loss-augmentation to the score (normalized Hamming distances used for loss)
    if (yi != null) {
      val l: Int = if (normalizedHammingLoss) yi.size else 1
      for (i <- 0 until xi.blockFeatures.cols) {
        thetaUnary(::, i) := thetaUnary(::, i) + 1.0 / l
        val idx = yi(i).toInt // Loss augmentation
        thetaUnary(idx, i) = thetaUnary(idx, i) - 1.0 / l
      }
    }

    decodeFn(thetaUnary, thetaPairwise)
  }


  def viterbiDecode(logNodePot: la.DenseMatrix[Double], logEdgePot: la.DenseMatrix[Double]): la.DenseVector[Double] = {

    val nBlocks  = logNodePot.cols

    val accCost = la.DenseMatrix.zeros[Double](2, nBlocks) // nx2 matrix
    val choices = la.DenseMatrix.zeros[Double](2, nBlocks) // nx2 matrix

    accCost(::, 0) := logNodePot(::, 0) // first column
    for (n <- 1 until nBlocks) {
      val M = {
        val accum = la.DenseVector.horzcat(accCost(::, n - 1),accCost(::, n - 1))
        val edge  = logEdgePot(::, n-1).toDenseMatrix.reshape(2,2).t
        accum + edge
      }
      val colMaxTmp = columnwiseMax(M)
      accCost(::, n) := logNodePot(::, n) + colMaxTmp(0, ::).t
      choices(::, n) := colMaxTmp(1, ::).t
    }

    val y = la.DenseVector.zeros[Double](nBlocks)
    y(nBlocks - 1) = la.argmax(accCost(::, nBlocks - 1).toDenseVector)
    for (n <- nBlocks - 2 to 0 by -1) {
      y(n) = choices(y(n + 1).toInt, n + 1)
    }
    y
  }

  override def oracleFn(weights: la.Vector[Double], x: PageFeatures, y: la.Vector[Double]): la.Vector[Double] =
    oracleFnWithDecode(weights, x, y, viterbiDecode)

  def predictFn(weights: la.Vector[Double], x: PageFeatures): la.Vector[Double] =
    oracleFn(weights, x, null)

  def lossFn(yPredicted: la.Vector[Double], yTruth: la.Vector[Double]): Double = {
    val sumIndivLoss = la.sum((yTruth :== yPredicted).map(x => if (x) 0 else 1)).toDouble
    if (normalizedHammingLoss) sumIndivLoss / yTruth.size
    else                       sumIndivLoss
  }

  /** Convert a Scala Vector to a Breeze DenseVector (fast) */
  private def dv(v: Vector[Double]): la.DenseVector[Double] = la.DenseVector(v.toArray)

  /**
   * Replicate the functionality of Matlab's max function
   * Returns 2-row vectors in the form a Matrix
   * 1st row contains column-wise max of the Matrix
   * 2nd row contains corresponding indices of the max elements
   */
  private def columnwiseMax(matM: la.Matrix[Double]): la.DenseMatrix[Double] = {
    val mat = matM.toDenseMatrix
    val colMax: la.DenseMatrix[Double] = la.DenseMatrix.zeros[Double](2, mat.cols)

    for (col <- 0 until mat.cols) {
      // 1st row contains max
      colMax(0, col) = la.max(mat(::, col))
      // 2nd row contains indices of the max
      colMax(1, col) = la.argmax(mat(::, col))
    }
    colMax
  }
}
