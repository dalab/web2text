package nl.tvogels.boilerplate.alignment

import nl.tvogels.boilerplate.utilities.Util
import jaligner.{Alignment => JAlignment,Sequence,SmithWatermanGotoh}
import jaligner.matrix.MatrixLoader
import scala.util.Random

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

  /** Number of times to try to find unique matches in source and cleaned */
  val splitTries = 1000

  /** Length of the substrings used for finding unique matches */
  val searchFragmentLength = 10

  /** Character inserted in the aligned text at the position of source
    * characters that have been deleted in the cleaned text */
  val GAPCHAR = String.valueOf(JAlignment.GAP)

  /** Alignment a source document with a cleaned document.
    *
    * @param source A string containing the source code
    * @param clean A string containing the cleaned text.
    *   It should not contain extra markup that was not in the
    *   original HTML document, and any spacing is best
    *   normalized to one space: ' '. See
    *   [[nl.tvogels.boilerplate.cleaneval.CleanEval.normalizeCleanFile]]
    *   as an example.
    * @return A string containing the cleaned page in the same
    * length as the input source, with `Alignment.GAPCHAR`
    * characters inserted where source-content has been removed.
    */
  def alignment(source: String, clean: String): String = {
    val mask = maskTags(source)
    assert(mask.length == source.length)

    var open: Vector[OpenSegment] = Vector(OpenSegment(0,clean.length, 0, mask.length))
    var matches: Vector[MatchSegment] = Vector()

    var i = 0
    while (i < splitTries) {
      i = i+1

      val curseg = Util.randomSelectionWeighted(open, open.map { x => (x.length - 10).max(0).toDouble })

      if (curseg.length - searchFragmentLength > 0) {
        val start = Random.nextInt(curseg.length - searchFragmentLength) + curseg.start
        val end   = start + searchFragmentLength
        val subs  = clean.substring(start,end)
        val res   = Util.allSubstringOccurences(mask,subs,curseg.sourceStart, curseg.sourceEnd - searchFragmentLength - 1)
        // println(s"For query $subs, I found ${res.length}")
        if (res.length == 1) {
          open    = open.filter(x => x != curseg) ++
                    Vector(
                      OpenSegment(curseg.start,start, curseg.sourceStart, res(0)),
                      OpenSegment(end,curseg.end, res(0)+searchFragmentLength, curseg.sourceEnd)
                     )
          matches = matches ++
                    Vector(MatchSegment(start,res(0),searchFragmentLength))
        }
      }
    }
    // println(matches sortBy { _.sourceStart })
    // println(open sortBy { _.sourceStart })
    val combo: Vector[SourceSegment] = open++matches
    val res = combo.sortBy { _.sourceStart }.map {
      case MatchSegment(start,sourceStart,length) => clean.substring(start,start+length)
      case curseg: OpenSegment => {
        println(s"Current segment: $curseg")
        // println("Source  : '" + curseg.takeSource(source) + "'")
        // println("Cleaned : '" + curseg.takeCleaned(clean) + "'")
        stringify(rawAlignment(curseg.takeSource(mask), curseg.takeCleaned(clean)),curseg.sourceLength)
      }
    }.mkString
    assert(res.length == source.length)
    res
  }

  /** A segment of source, corresponding to a segment in the cleaned file */
  private sealed trait SourceSegment {

    /** Start position in the cleaned document */
    def start: Int

    /** Start position in the source document */
    def sourceStart: Int

    /** Length of the matching piece (in the cleaned document) */
    def length: Int
  }

  /** An open segment of source that is to be matched
    *
    * @param start Start position in the cleaned document
    * @param end End position in the cleaned document
    * @param sourceStart Start position in the source document
    * @param sourceEnd End position in the source document
    */
  private case class OpenSegment(val start: Int, end: Int, sourceStart: Int, sourceEnd: Int) extends SourceSegment {

    /** Length of the segment in the cleaned version */
    def length = end - start

    /** Length of the segment in the source */
    def sourceLength = sourceEnd - sourceStart

    /** Take the corresponding substring from the cleaned document
      * @param cleaned Cleaned string
      */

    def takeCleaned(cleaned: String) = cleaned.substring(start,end)

    /** Take the corresponding substring from the source document
      * @param source Source string
      */
    def takeSource(source: String) = source.substring(sourceStart,sourceEnd)

  }

  /** A segment that is already matched among source and cleaned
    *
    * @param start Start position in the cleaned document
    * @param sourceStart Start position in the source document
    * @param length Length in both documents
    */
  private case class MatchSegment(val start: Int, sourceStart: Int, length: Int) extends SourceSegment

  /** Format a JAlignment output to a sting of the length of
    * the source document, containing `GAPCHAR` characters
    * at the position where characters have been removed in
    * the cleaned document.
    *
    * @param align `JAlignment` output
    * @param maskLength Length of the source segment to be aligned
    */
  private def stringify(align: JAlignment, masklength: Int) = {
    val a: String = String.valueOf(align.getSequence1)
    val b: String = String.valueOf(align.getSequence2)
    val gap = GAPCHAR
    val c: String = b.zipWithIndex.filter(x => a(x._2) != JAlignment.GAP).unzip._1.mkString
    val d = gap * align.getStart1 + c
    d + gap * (masklength - d.length)
  }

  /** Find raw alignment with `JAligner`
    * @param a Segment in source document
    * @param b Segment in cleaned document
    */
  private def rawAlignment(a: String, b: String) = {
    val x = new Sequence(removeStrangeSymbols(a))
    val y = new Sequence(removeStrangeSymbols(b))
    Util.save("/Users/thijs/Desktop/x.txt",removeStrangeSymbols(a))
    Util.save("/Users/thijs/Desktop/y.txt",removeStrangeSymbols(b))
    SmithWatermanGotoh.align(x, y, alignmentMatrix, 0.5f, 0f)
  }

  /** Matrix with matching/replacement costs used by `JAligner` */
  lazy val alignmentMatrix =
    MatrixLoader.load(
      new jaligner.ui.filechooser.NamedInputStream(
        "ALIGNMENT_MATRIX",
        getClass().getResourceAsStream("/ALIGNMENT_MATRIX")
      )
    )

  /** Remove symbols that are not supported by `JAligner`
    * @param x Input string
    */
  private def removeStrangeSymbols(x: String) = {
    // x.replaceAll("""[^A-Za-z0-9\-_;:\?.',#\(\)\*']""","X")
    x.replaceAll("""[^\x00-\x7F]""","X")
  }

  /** Mask tags in an HTML document by #-characters, such
    * that they won't match with the cleaned document.
    */
  private def maskTags(html: String): String = {
    val htmlTags = """(?s)<(.*?)>"""r
    val fmTags = """(?s)(<HEAD[^>]*>.*?</HEAD[^>]*>|<SCRIPT[^>]*>.*?</SCRIPT[^>]*>|<STYLE[^>]*>.*?</STYLE[^>]*>|<!--.*?-->|&[A-Z]+;|<[^<>]*?>)"""r

    val html2 = html.toUpperCase.replaceAll("""\s"""," ")
    fmTags.replaceAllIn(html2, m => "#" * m.group(0).length)
          // .replaceAll("""[^\x00-\x7F]"""," ")
          //.replaceAll("""\W"""," ")
          .replaceAllLiterally(String.valueOf(JAlignment.GAP), " ")
  }

}