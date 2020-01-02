package ch.ethz.dalab.web2text.classification

/** Class to hold performance statistics from a classifier on a dataset */
case class PerformanceStatistics(accuracy: Double, precision: Double, recall: Double, f1: Double) {
  override def toString = {
    f"PerformanceStatistics(accuracy=$accuracy%1.2f, precision=$precision%1.2f, recall=$recall%1.2f, F1=$f1%1.2f)"
  }
}

/** Factory for PerforamanceStatistics */
object PerformanceStatistics {

  /** Generate performance statistics from a sequence of (predicted, real value)-pairs
    * @param pairs vector of (predicted value, real value)-pairs
    * @return PerformanceStatistics */
  def fromPairs(pairs: Seq[(Int,Int)]): PerformanceStatistics = {
    val n = pairs.length
    var truePos, falsePos, trueNeg, falseNeg = 0.0
    pairs foreach {
      case (1,1) => truePos  += 1
      case (1,0) => falsePos += 1
      case (0,1) => falseNeg += 1
      case (0,0) => trueNeg  += 1
    }
    if (n==0) return PerformanceStatistics(0,0,0,0)

    val accuracy  = (truePos+trueNeg).toDouble / n

    val precision = if ((truePos+falsePos) != 0)
                      truePos.toDouble / (truePos+falsePos)
                    else
                      0
    val recall    = if ((truePos+falseNeg) != 0)
                      truePos.toDouble / (truePos+falseNeg)
                    else
                      0
    val f1        = if ((precision+recall) != 0)
                      2 * precision * recall / (precision+recall)
                    else
                      0

    PerformanceStatistics(accuracy,precision,recall,f1)
  }

}
