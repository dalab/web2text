package nl.tvogels.boilerplate.cdom

import org.jsoup.{nodes => jnodes}
import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.utilities.Settings

/** Node properties
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
case class NodeProperties (
  var nCharacters: Int,
  var nWords: Int,
  var nSentences: Int,
  var nPunctuation: Int,
  var nDashes: Int,
  var nStopwords: Int,
  var nWordsWithCapital: Int,
  var nCharsInLink: Int,
  var totalWordLength: Int,
  var endsWithPunctuation: Boolean,
  var endsWithQuestionMark: Boolean,
  var startPosition: Int,
  var endPosition: Int,
  var nChildrenDeep: Int,
  var blockBreakBefore: Boolean,
  var blockBreakAfter: Boolean,
  var brBefore: Boolean,
  var brAfter: Boolean
) {
  def toHTML: String = s"""
  |<dl>
  |  <dt>nCharacters</dt><dd>$nCharacters</dd>
  |  <dt>nWords</dt><dd>$nWords</dd>
  |  <dt>nSentences</dt><dd>$nSentences</dd>
  |  <dt>nPunctuation</dt><dd>$nPunctuation</dd>
  |  <dt>nDashes</dt><dd>$nDashes</dd>
  |  <dt>nStopwords</dt><dd>$nStopwords</dd>
  |  <dt>nWordsWithCapital</dt><dd>$nWordsWithCapital</dd>
  |  <dt>nCharsInLink</dt><dd>$nCharsInLink</dd>
  |  <dt>totalWordLength</dt><dd>$totalWordLength</dd>
  |  <dt>endsWithPunctuation</dt><dd>$endsWithPunctuation</dd>
  |  <dt>endsWithQuestionMark</dt><dd>$endsWithQuestionMark</dd>
  |  <dt>startPosition</dt><dd>$startPosition</dd>
  |  <dt>endPosition</dt><dd>$endPosition</dd>
  |  <dt>nChildrenDeep</dt><dd>$nChildrenDeep</dd>
  |  <dt>blockBreakBefore</dt><dd>$blockBreakBefore</dd>
  |  <dt>blockBreakAfter</dt><dd>$blockBreakAfter</dd>
  |  <dt>brBefore</dt><dd>$brBefore</dd>
  |  <dt>brAfter</dt><dd>$brAfter</dd>
  |</dl>""".stripMargin
}

/** Factory for the [[nl.tvogels.boilerplate.cdom.NodeProperties]] class */
object NodeProperties {

  /** Create a NodeProperties object from a domnode and its children.
    *
    * If there are no children, it is about a leaf
    * If there is one child, it merges the features,
    * and if there are multiple children, add up the stuff (in most cases)
    */
  def fromNode(domnode: jnodes.Node, children: Seq[Node]): NodeProperties =
    children.length match {

      // If there are no children, this is a leaf
      case 0 => {

        val s = Settings
        val text = DOM.text(domnode).trim
        val words = text.split("\\W+").filter(x => x.length > 0)
        val sentences = Util.splitSentences(text)

        NodeProperties(
          nCharacters           = scala.math.max(text.length,1),
          nWords                = words.length,
          nSentences            = sentences.length,
          nPunctuation          = text.count { x => s.punctuation.contains(x) },
          nDashes               = text.count { x => s.dashes.contains(x) },
          nStopwords            = words.count { x => s.stopwords.contains(x) },
          nWordsWithCapital     = words.count { _.charAt(0).isUpper },
          nCharsInLink          = if (domnode.nodeName == "a") text.length else 0,
          totalWordLength       = words.view.map(_.length).sum,
          endsWithPunctuation   = if (text.length == 0) false
                                  else s.punctuation.contains(text.last),
          endsWithQuestionMark  = if (text.length == 0) false
                                  else (text.last == '?'),
          startPosition         = domnode.startPosition,
          endPosition           = domnode.endPosition,
          nChildrenDeep         = 0,
          blockBreakBefore      = domnode.previousSibling != null &&
                                  Settings.blockTags.contains
                                   (domnode.previousSibling.nodeName),
          blockBreakAfter       = domnode.nextSibling != null &&
                                  Settings.blockTags.contains
                                   (domnode.nextSibling.nodeName),
          brBefore              = domnode.previousSibling != null &&
                                  domnode.previousSibling.nodeName == "br",
          brAfter               = domnode.nextSibling != null &&
                                  domnode.nextSibling.nodeName == "br"
        )
      }

      // If there is one child, merge the features
      case 1 => {

        var cfeat = children.head.properties
        val tag = domnode.nodeName
        val prevtag = Option(domnode.previousSibling).map(_.nodeName) getOrElse "[none]"
        val nexttag = Option(domnode.nextSibling).map(_.nodeName) getOrElse "[none]"

        assert(
          cfeat != null,
          "We cannot initialize features from a child, if the child doesn't have them"
        )

        if (Settings.blockTags.contains(tag) || Settings.blockTags.contains(prevtag)) {
          propagateDownBlockTagLeft(children)
        }
        if (Settings.blockTags.contains(tag) || Settings.blockTags.contains(nexttag)) {
          propagateDownBlockTagRight(children)
        }

        NodeProperties(
          nCharacters           = cfeat.nCharacters,
          nWords                = cfeat.nWords,
          nSentences            = cfeat.nSentences,
          nPunctuation          = cfeat.nPunctuation,
          nDashes               = cfeat.nDashes,
          nStopwords            = cfeat.nStopwords,
          nWordsWithCapital     = cfeat.nWordsWithCapital,
          nCharsInLink          = if (domnode.nodeName == "a")
                                    cfeat.nCharacters
                                  else
                                    cfeat.nCharsInLink,
          totalWordLength       = cfeat.totalWordLength,
          endsWithPunctuation   = cfeat.endsWithPunctuation,
          endsWithQuestionMark  = cfeat.endsWithQuestionMark,
          startPosition         = if (cfeat.startPosition > -1)
                                    cfeat.startPosition
                                  else
                                    domnode.startPosition,
          endPosition           = if (cfeat.endPosition > -1)
                                    cfeat.endPosition
                                  else
                                    domnode.endPosition,
          nChildrenDeep         = cfeat.nChildrenDeep,
          blockBreakBefore      = Settings.blockTags.contains(tag) ||
                                  Settings.blockTags.contains(prevtag),
          blockBreakAfter       = Settings.blockTags.contains(tag) ||
                                  Settings.blockTags.contains(nexttag),
          brBefore              = cfeat.brBefore ||
                                  (domnode.previousSibling != null &&
                                   domnode.previousSibling.nodeName == "br"),
          brAfter               = cfeat.brAfter ||
                                  (domnode.nextSibling != null &&
                                   domnode.nextSibling.nodeName == "br")
        )
      }

      // If there are multiple children, add up the stuff (in most cases)
      case _ => { // >= 2
        // Collect and rename some variables for convenience
        var cfeat     = children.map(_.properties)
        var tag       = domnode.nodeName
        val prevtag = Option(domnode.previousSibling).map(_.nodeName) getOrElse "[none]"
        val nexttag = Option(domnode.nextSibling).map(_.nodeName) getOrElse "[none]"


        if (Settings.blockTags.contains(tag) || Settings.blockTags.contains(prevtag)) {
          propagateDownBlockTagLeft(children)
        }
        if (Settings.blockTags.contains(tag) || Settings.blockTags.contains(nexttag)) {
          propagateDownBlockTagRight(children)
        }

        // Initialize the features to their neural values
        val features = NodeProperties(
          nCharacters=0, nWords=0, nSentences=0, nPunctuation=0, nDashes=0,
          nStopwords=0, nWordsWithCapital=0, totalWordLength=0, nChildrenDeep=cfeat.length,
          nCharsInLink          = 0,
          endsWithPunctuation   = cfeat.last.endsWithPunctuation,
          endsWithQuestionMark  = cfeat.last.endsWithQuestionMark,
          startPosition         = cfeat.head.startPosition,
          endPosition           = cfeat.last.endPosition,
          blockBreakBefore      = Settings.blockTags.contains(tag) ||
                                  Settings.blockTags.contains(prevtag),
          blockBreakAfter       = Settings.blockTags.contains(tag) ||
                                  Settings.blockTags.contains(nexttag),
          brBefore              = prevtag == "br",
          brAfter               = nexttag == "br"
        )

        // Update the features, by summing up things
        cfeat.foreach(x => {
          features.nCharacters        += x.nCharacters
          features.nWords             += x.nWords
          features.nSentences         += x.nSentences
          features.nPunctuation       += x.nPunctuation
          features.nDashes            += x.nDashes
          features.nStopwords         += x.nStopwords
          features.nWordsWithCapital  += x.nWordsWithCapital
          features.totalWordLength    += x.totalWordLength
          features.nChildrenDeep      += x.nChildrenDeep
          features.nCharsInLink       += (if (tag == "a") x.nCharacters
                                          else x.nCharsInLink)
        })

        features
      }
    }

    /** Set block break to true for a node's children (left), recursively. */
    def propagateDownBlockTagLeft(children: Seq[Node]): Unit = {
      // on the left
      var childs = children
      while(childs.length > 0) {
        val left = childs.head
        if (left.properties.blockBreakBefore == true) return
        left.properties.blockBreakBefore = true
        childs = left.children
      }
    }
    /** Set block break to true for a node's children (right), recursively. */
    def propagateDownBlockTagRight(children: Seq[Node]): Unit = {
      // on the left
      var childs = children
      while(childs.length > 0) {
        val right = childs.last
        if (right.properties.blockBreakAfter == true) return
        right.properties.blockBreakAfter = true
        childs = right.children
      }
    }
}