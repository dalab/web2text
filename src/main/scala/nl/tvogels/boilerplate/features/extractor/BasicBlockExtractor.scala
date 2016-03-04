package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor
import nl.tvogels.boilerplate.utilities.Util
import scala.math.log

object BasicBlockExtractor extends BlockFeatureExtractor {

  def apply(cdom: CDOM) = (node: Node) => {
    val p = node.properties
    val relPos = (0.5*(p.endPosition+p.startPosition).toDouble/cdom.root.properties.endPosition)
    Vector(
      if (p.nCharacters > 0) (p.nCharsInLink.toDouble / p.nCharacters) else (0.0),
      if (p.nWords > 0) (p.nStopwords.toDouble / p.nWords) else (0.0),
      log(p.nCharacters),
      if (p.nCharsInLink > 0) (p.nPunctuation.toDouble / p.nCharacters) else 0.0,
      if (p.nCharsInLink > 0) (p.nNumeric.toDouble / p.nCharacters) else 0.0,
      if (p.nSentences > 0) log((p.nCharacters.toDouble / p.nSentences)) else 0.0,
      // p.nCharacters.toDouble / cdom.root.properties.nCharacters,
      relPos,
      relPos*relPos,
      if (p.endsWithPunctuation) 1.0 else 0.0,
      if (p.endsWithQuestionMark) 1.0 else 0.0,
      if (p.containsCopyright) 1.0 else 0.0,
      if (p.containsEmail) 1.0 else 0.0,
      if (p.containsUrl) 1.0 else 0.0,
      if (p.containsYear) 1.0 else 0.0,
      if (p.nWords > 0) p.nWordsWithCapital.toDouble / p.nWords else 0.0
    )
  }

  val labels = Vector(
    "link_density",
    "stopword_ratio",
    "log_n_characters",
    "punctuation_ratio",
    "numeric_ratio",
    "log_avg_sentence_length",
    "relative_position",
    "relative_position^2",
    "ends_with_punctuation",
    "ends_with_question_mark",
    "contains_copyright",
    "contains_email",
    "contains_url",
    "contains_year",
    "ratio_words_with_capital"
  )


}