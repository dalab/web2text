package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor
import nl.tvogels.boilerplate.utilities.Util

object BasicBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM) = (node: Node) => {
    val p = node.properties
    Vector(
      if (p.nCharacters > 0) (p.nCharsInLink.toDouble / p.nCharacters) else (0.0),
      if (p.nWords > 0) (p.nStopwords.toDouble / p.nWords) else (0.0),
      scala.math.log(p.nCharacters),
      if (p.nCharsInLink > 0) (p.nPunctuation.toDouble / p.nCharacters) else 0.0,
      // p.nCharacters.toDouble / cdom.root.properties.nCharacters,
      (0.5*(p.endPosition+p.startPosition).toDouble/cdom.root.properties.endPosition),
      if (p.endsWithPunctuation) 1.0 else 0.0,
      if (p.endsWithQuestionMark) 1.0 else 0.0,
      if (p.nWords > 0) p.nWordsWithCapital.toDouble / p.nWords else 0.0
    )
  }

  val labels = Vector("link_density","stopword_ratio","log_n_characters","punctuation_ratio","relative_position","ends_with_punctuation","ends_with_question_mark","ratio_words_with_capital")


}