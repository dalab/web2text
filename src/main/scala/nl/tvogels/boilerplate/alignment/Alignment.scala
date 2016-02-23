package nl.tvogels.boilerplate.alignment

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.cdom.CDOM
import nl.tvogels.boilerplate.cdom.NodeProperties
import scala.util.Random
import breeze.linalg
import scala.collection.mutable.ArrayBuffer

/** Alignment of a cleaned page with the original HTML source
  *
  * Given an cleaned webpage, the text of which was generated
  * from the HTML source by removals of main text only, this class
  * takes care of aligning the cleaned page with the original page.
  * The output is a page of the same length as the source, with
  * `Alignment.GAPCHAR`-characters inserted at the positions where
  * a character was removed in the cleaned version.
  *
  * At the base of the algorithm is a dynamic programming sequence
  * algorithm. To speed it up, we first randomly search for
  * possible substrings of the cleaned text that occur in the source
  * uniquely. Unique matches are definitely a match and split the
  * sequence alignment problem in half. We try `splitTries` times to
  * find unique matches of length `searchFragmentLength`.
  *
  * Main method [[nl.tvogels.boilerplate.alignment.Alignment.alignment]]
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
object Alignment {

  // val GAPCHAR = "空" // `empty' in Mandarin
  val GAPCHAR = "□"

  /** A segment of source, corresponding to a segment in the cleaned file */
  private sealed trait SegmentPair {
    /** Start position (inclusive), endposition (exclusive) in source
      * e.g.(0,3) = [0,1,2] */
    val source: (Int,Int)
    /** Start position (inclusive), endposition (exclusive) in cleaned text
      * e.g.(0,3) = [0,1,2] */
    val clean: (Int,Int)

    val sourceLength = source._2 - source._1
    val cleanLength = clean._2 - clean._1
  }

  /** An open segment of source that is to be matched */
  private case class OpenSegment(source:(Int,Int),clean:(Int,Int)) extends SegmentPair

  /** A segment that is already matched among source and cleaned */
  private case class MatchedSegment(source:(Int,Int),clean:(Int,Int)) extends SegmentPair {
    assert(sourceLength == cleanLength, s"Source length $sourceLength is not equal to clean length $cleanLength")
  }


  private def find1to1mathches(source: String, cleaned: String, k: Int): ArrayBuffer[SegmentPair] = {

    // Define output segment list (mutable)
    val list: ArrayBuffer[SegmentPair] = ArrayBuffer()

    // Input lenghts
    val n = source.length
    val m = cleaned.length

    // construct a map of substrings of length k and their location in the source (source)
    // sourceMap(substring) = Vector(startloc1,loc2,loc3,...)
    val sourceMap = (for (i <- 0 until n+1-k;
                       slice = source.slice(i,i+k);
                       if !slice.contains(GAPCHAR)) yield (slice,i))
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .toMap

    // for safety, we also need a document that is stripped of any gap-chars and whitespace
    val trimFilter = ((c: Char) => !Character.isWhitespace(c) && c.toString != Alignment.GAPCHAR)
    val trimmedSource = source.filter(trimFilter)
    val trimmedSourceMaps = (1 to k) map {
      case k => (for (i <- 0 until trimmedSource.length+1-k) yield (trimmedSource.slice(i,i+k),i))
                  .groupBy(_._1)
                  .mapValues(_.length)
                  .toMap
    }

    // test whether two characters are equal enough
    def equalEnough(c1: Char, c2: Char) = {
      (Character.isWhitespace(c1) && Character.isWhitespace(c2)) ||
      (Character.toUpperCase(c1) == Character.toUpperCase(c2))
    }

    // Loop through the cleaned version and construct matches
    // val buf = new StringBuilder
    var lastMatch: SegmentPair = MatchedSegment((0,0),(0,0))
    var justPopped = false // have we already removed a potentially suspicious match?
    var i = 0 // current position,
    while (i < m+1-k) {
      val subs = cleaned.slice(i,i+k)

      // compute trimmed substring of length k
      var trimmedSubs = subs.filter(trimFilter)
      // var ind = i+k
      // while(trimmedSubs.length < k && ind < m) {
      //   val newChar = cleaned(ind)
      //   if (trimFilter(newChar)) {
      //     trimmedSubs += newChar
      //   }
      //   ind += 1
      // }

      val matchLocations = sourceMap.getOrElse(subs,Vector())
      val trimmedMatchCount = if (trimmedSubs.length > 0)
                                trimmedSourceMaps(trimmedSubs.length-1)
                                  .getOrElse(trimmedSubs.toString,0)
                              else 0
      // println(s"'${subs.replaceAll("\n","X")}' : $matchLocations")
      if (matchLocations.length == 1 && trimmedMatchCount == 1) { // there is a 1-1 match!
        val sourcePos = matchLocations.head
        // Figure out how far we can extend the match to the right
        var extraRight = 0
        while (i + k + extraRight < m
               && sourcePos + k + extraRight < n
               && equalEnough(cleaned(i+k+extraRight), source(sourcePos+k+extraRight))) {
          extraRight += 1
        }
        // Figure out how far we can extend the match to the left
        var extraLeft = 0
        while (i-extraLeft > 0
               && sourcePos-extraLeft > 0
               && sourcePos-extraLeft >= lastMatch.source._2+1
               && equalEnough(cleaned(i-1-extraLeft), source(sourcePos-1-extraLeft))) {
          extraLeft += 1
        }
        // If the current piece is before the previous piece, discard both
        // println("Howdy")
        if (sourcePos <= lastMatch.source._1) {
          // println ("Have to skip!")
          // println(s"Prev: '${source.slice(list.last.source._1,list.last.source._2)}'")
          // println(s"Now:  '$subs' / '${source.slice(sourcePos-extraLeft,sourcePos+k+extraRight)}'")
          // if (!justPopped) {
            // remove last two elements from the list
            val llength = list.length
            list.remove(llength-2,2)
            lastMatch = if (llength >= 3) list(llength-3) else MatchedSegment((0,0),(0,0))
            justPopped=true
          // }
          // Don't do anything with the current match
          i +=1
        } else if (sourcePos < lastMatch.source._2) {
          // Skip but its not too bad
          i += 1
        } else {
          justPopped=false
          // If it extends too far to the left, shorten it
          while(sourcePos-extraLeft < lastMatch.source._2) {
            extraLeft -= 1
          }

          // Append a non-matched piece to the list
          if (sourcePos-extraLeft > lastMatch.source._2 || i-extraLeft > lastMatch.clean._2) {
            assert(sourcePos-extraLeft - lastMatch.source._2 >=0, s"Open segment reversed for subs '$subs'")
            list += OpenSegment(
              source = (lastMatch.source._2, sourcePos - extraLeft),
              clean  = (lastMatch.clean._2,  i - extraLeft)
            )
          }
          // Append the match segment
          // println(list mkString "\n")
          // println("LM: "+lastMatch)
          // println(s"Extra left: $extraLeft")
          assert(sourcePos + k + extraRight - (sourcePos-extraLeft) >=0, s"Matched segment reversed for subs '$subs'")
          lastMatch = MatchedSegment(
            source=(sourcePos-extraLeft,sourcePos + k + extraRight),
            clean=(i-extraLeft,i + k + extraRight)
          )
          list += lastMatch
          // println(list mkString "\n")
          i += k + extraRight
        }

      } else {
        // There is no 1-1 match, continue
        i += 1
      }
    }
    // Append a final non-match at the end
    if (list.length > 0 && list.last.source._2 < source.length) {
      list += OpenSegment(
        source=(list.last.source._2,source.length),
        clean=(list.last.clean._2,cleaned.length)
      )
    } else if(list.length == 0) {
      list += OpenSegment(
        source=(0,source.length),
        clean=(0,cleaned.length)
      )
    }

    list
  }

  def alignment(source: String, cleaned: String): String = {
    val mask = maskTags(source)

    val segments = find1to1mathches(mask, cleaned, k=10)

    val output = segments map {
      case OpenSegment(source, clean) => {
        // println(s"OPEN:  $source '${mask.slice(source._1,source._2).replaceAll("\n","X")}'  /  '${cleaned.slice(clean._1,clean._2).replaceAll("\n","X")}'\n")
        dpalignment(
          source = mask.slice(source._1,source._2),
          cleaned = cleaned.slice(clean._1,clean._2)
        )
      }
      case MatchedSegment(source, clean) => {
        // println(s"MATCH: $source '${cleaned.slice(clean._1,clean._2).replaceAll("\n","X")}'\n")
        cleaned.slice(clean._1,clean._2)
      }
    }

    output mkString ""
  }

  /** Apply dynamic programming for the alignment
    * A perfect match gets three points, if it is a letter or digit, and 1 point otherwise
    * Skipping a letter in the clean text has a penalty of -6, or 0 if it is whitespace
    * Skipping letters in the source is free, but there is a gap-start penalty of -1 */
  def dpalignment(source: String, cleaned: String): String = {


    /** Enum type for a decision in the dynamic programming algorithm */
    object Decision extends Enumeration {
      type Decision = Value
      val Match, SkipSource, SkipClean = Value
    }
    import Decision._

    def score(decision: Decision, sourceChar: Char, cleanedChar: Char, isInGap: Boolean) = decision match {
      case Match => if (Character.isLetterOrDigit(sourceChar) &&
                        sourceChar == cleanedChar) 3
                    else if (Character.toUpperCase(sourceChar) == Character.toUpperCase(cleanedChar)) 1
                    else 0
      case SkipClean => if (Character.isWhitespace(cleanedChar)) 0 else -6
      case SkipSource => if (isInGap) 0 else -2
    }

    // Convert strings to lists of characters

    // Store lengths
    val n = source.length
    val m = cleaned.length

    /** Score matrix */
    val S = new linalg.DenseMatrix[Int](2,m+1)
    /** Gap matrix */
    val G = new linalg.DenseMatrix[Boolean](2,m+1)
    /** Decisions matrix */
    val D = new linalg.DenseMatrix[Decision](n+1,m+1)

    // Initialize first row and column
    for (j <- 0 to m) { S(0,j) = -j; G(0,j)=true; D(0,j)=SkipClean }
    for (i <- 0 to 1) { G(i,0)=true }
    for (i <- 0 to n) { D(i,0)=SkipSource }
    D(0,0) = null

    // Compute costs in matrix and store decision
    for(i <- 1 to n;
        j <- 1 to m) {
      val sourceChar = source(i-1)
      val cleanedChar = cleaned(j-1)

      val skipCleanScore  =
        S(i%2,j-1) + score(SkipClean, sourceChar, cleanedChar, G(i%2,j-1))
      val skipSourceScore =
        S((i-1)%2,j) + score(SkipSource, sourceChar, cleanedChar, G((i-1)%2,j))

      var bestScore: Int = 0
      var bestDecision: Decision = null

      if(skipCleanScore > skipSourceScore) {
        bestScore = skipCleanScore
        bestDecision = SkipClean
      } else {
        bestScore = skipSourceScore
        bestDecision = SkipSource
      }

      if (Character.toUpperCase(sourceChar) == Character.toUpperCase(cleanedChar) ||
          (Character.isWhitespace(sourceChar) && Character.isWhitespace(cleanedChar))) {
        val matchScore = S((i-1)%2,j-1) + score(Match, sourceChar, cleanedChar, isInGap=false)
        if (matchScore > bestScore) {
          bestScore = matchScore
          bestDecision = Match
        }
      }

      D(i,j) = bestDecision
      S(i%2,j) = bestScore
      G(i%2,j) = bestDecision match {
        case Match => false
        case SkipClean => G(i%2,j-1)
        case SkipSource => true
      }

    }

    // construct string
    val ret = new StringBuilder
    var i = n
    var j = m
    while (i != 0 || j != 0)
      D(i,j) match {
        case Match => { ret ++= source(i-1).toString; i -= 1; j -= 1 }
        case SkipClean => { j -= 1 }
        case SkipSource=> { ret ++= Alignment.GAPCHAR; i -= 1 }
      }

    val str = ret.reverse.toString
    assert(str.length == source.length,
      "Output should be of the same length as the input source.")
    str

  }


  /** Mask tags in an HTML document by #-characters, such
    * that they won't match with the cleaned document.
    */
  def maskTags(html: String): String = {
    // val htmlTags = """(?s)(?i)(<[a-z]+(.*?)>)"""r
    val fmTags = """(?s)(?i)(<HEAD[^>]*>.*?</HEAD[^>]*>|<SCRIPT[^>]*>.*?</SCRIPT[^>]*>|<STYLE[^>]*>.*?</STYLE[^>]*>|<!--.*?-->|&[A-Z]+;|</?[a-z]+[^<>]*?>)"""r

    fmTags.replaceAllIn(html, m => Alignment.GAPCHAR * m.group(0).length)
    // htmlTags.replaceAllIn(html2,m => Alignment.GAPCHAR * m.group(0).length)
  }


  /** Extract 'ground truth' labels from a cleaned file in which the characters are perfectly
    * aligned with the source document */
  def labelsFromAlignedString(cdom: CDOM, aligned: String): Vector[Int] =
    cdom.leaves map {
      node => labelFromAlignedString(node.properties, aligned)
    }

  /** Get a label from an aligned string
    * @param position start and end position pair */
  private def labelFromAlignedString(props: NodeProperties, aligned: String) = (props.startPosition, props.endPosition) match {
    case (-1,_) =>                       println(Console.RED+s"-1 node.startPosition value"+Console.RESET); 0
    case (_,-1) =>                       println(Console.RED+s"-1 node.endPosition value"+Console.RESET); 0
    case (s,_) if s < 0 =>               println(Console.RED+s"negative node.startPosition value"+Console.RESET); 0
    case (_,e) if e > aligned.length =>  println(Console.RED+s"node.endPosition $e falls outside document (${aligned.length})"+Console.RESET); 0
    case (s,e) =>                        {
      val subs = aligned.substring(s,e)
      val found = subs.count(x => x.toString != Alignment.GAPCHAR)
      if (found > 2*props.nCharacters/3) { // intentional floor
        1
      } else 0
    }
  }

}