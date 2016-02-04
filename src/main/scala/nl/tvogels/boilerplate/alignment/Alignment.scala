package nl.tvogels.boilerplate.alignment

import nl.tvogels.boilerplate.utilities.Util
import jaligner.{Alignment => JAlignment,Sequence,SmithWatermanGotoh}
import jaligner.matrix.MatrixLoader
import scala.util.Random

object Alignment {

  val splitTries = 1000
  val searchFragmentLength = 10
  val GAPCHAR = String.valueOf(JAlignment.GAP)

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
    println(open sortBy { _.sourceStart })
    val combo: Vector[SourceSegment] = open++matches
    val res = combo.sortBy { _.sourceStart }.map {
      case MatchSegment(start,sourceStart,length) => clean.substring(start,start+length)
      case curseg: OpenSegment => {
        println(curseg)
        // println("Source  : '" + curseg.takeSource(source) + "'")
        // println("Cleaned : '" + curseg.takeCleaned(clean) + "'")
        stringify(rawAlignment(curseg.takeSource(mask), curseg.takeCleaned(clean)),curseg.sourceLength)
      }
    }.mkString
    assert(res.length == source.length)
    res
  }


  private sealed trait SourceSegment {
    def start: Int
    def sourceStart: Int
    def length: Int
  }

  private case class OpenSegment(val start: Int, end: Int, sourceStart: Int, sourceEnd: Int) extends SourceSegment {
    def length = end - start
    def sourceLength = sourceEnd - sourceStart
    def takeCleaned(cleaned: String) = cleaned.substring(start,end)
    def takeSource(source: String) = source.substring(sourceStart,sourceEnd)
  }

  private case class MatchSegment(val start: Int, sourceStart: Int, length: Int) extends SourceSegment


  private def stringify(align: JAlignment, masklength: Int) = {
    val a: String = String.valueOf(align.getSequence1)
    val b: String = String.valueOf(align.getSequence2)
    val gap = GAPCHAR
    val c: String = b.zipWithIndex.filter(x => a(x._2) != JAlignment.GAP).unzip._1.mkString
    val d = gap * align.getStart1 + c
    d + gap * (masklength - d.length)
  }

  private def rawAlignment(a: String, b: String) = {
    val x = new Sequence(removeStrangeSymbols(a))
    val y = new Sequence(removeStrangeSymbols(b))
    Util.save("/Users/thijs/Desktop/x.txt",removeStrangeSymbols(a))
    Util.save("/Users/thijs/Desktop/y.txt",removeStrangeSymbols(b))
    SmithWatermanGotoh.align(x, y, alignmentMatrix, 0.5f, 0f)
  }

  lazy val alignmentMatrix =
    MatrixLoader.load(
      new jaligner.ui.filechooser.NamedInputStream(
        "ALIGNMENT_MATRIX",
        getClass().getResourceAsStream("/ALIGNMENT_MATRIX")
      )
    )

  private def removeStrangeSymbols(x: String) = {
    // x.replaceAll("""[^A-Za-z0-9\-_;:\?.',#\(\)\*']""","X")
    x.replaceAll("""[^\x00-\x7F]""","X")
  }

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