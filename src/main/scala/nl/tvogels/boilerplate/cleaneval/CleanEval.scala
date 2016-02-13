package nl.tvogels.boilerplate.cleaneval

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.features.{FeatureExtractor,PageFeatures}
import nl.tvogels.boilerplate.cdom.CDOM
import nl.tvogels.boilerplate.classification.PerformanceStatistics
import org.jsoup.Jsoup

/** CleanEval related functionality
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
object CleanEval {

  /** Directory within `src/main/resources` in which to find the cleaneval data */
  val directory = "/cleaneval"

  /** Get path to the resource of the HTML source for document #n */
  def origPath(n: Int) = s"$directory/orig/$n.html"

  /** Get path to the resource of the cleaned document (gold standard) for document #n */
  def cleanPath(n: Int) = s"$directory/clean/$n.txt"

  /** Get path to the resource of the cleaned aligned document for document #n.
    * @see [[nl.tvogels.boilerplate.alignment.Alignment]]. */
  def alignedPath(n: Int) = s"$directory/aligned/$n.txt"

  /** Vector of items that could not be aligned */
  val skipped: Vector[Int] = Vector(638)

  /** Case class for a CleanEval page containing all available information on it.
    * @param id ID
    * @param orig Source document
    * @param clean Cleaned document
    * @param aligned Aligned document
    */
  case class Page(id: Integer, orig: String, clean: String, aligned: String) {

    private val origFormat = """(?s)<text id="(.*?)".*?encoding="(.*?)">(.*)</text>[\p{Z}\s]*""".r

    /** Split the original page in url, encoding, and HTML content.
      * Watch out! HTML content has a different length than the source document. */
    val (url: String, encoding: String, html: String) = orig match {
      case origFormat(url,encoding,html) => (url, encoding, html)
    }

    /** Take out the text tage from the source document */
    val textTagFormat = """(?s)(<text[^>]*>|</text>)""".r

    /** Original page, but with the <text> tags replaced by equally long series of spaces, to aid the parser */
    lazy val origWithoutTextTag = textTagFormat.replaceAllIn(orig, m => " " * m.group(0).length)
    assert(origWithoutTextTag.length == orig.length)

    /** Unique ID across datasets */
    lazy val docId = f"cleaneval_$id%03d"

  }

  /** Vector of indices of CleanEval items that are complete with source, cleaned and aligned versions. */
  val indices: Vector[Int] = Vector(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,292,293,294,295,296,297,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,338,339,340,341,342,343,344,345,346,347,348,349,350,351,352,353,354,355,356,357,358,359,360,361,362,363,364,365,366,367,368,369,370,371,372,373,374,375,376,377,378,379,380,381,382,383,384,385,386,387,388,389,390,391,392,393,394,395,396,397,398,399,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,421,422,424,425,426,427,428,429,430,431,433,434,435,436,437,438,439,440,441,442,443,444,445,446,447,448,449,450,451,452,453,454,455,456,457,458,459,460,461,462,463,464,465,466,467,468,469,470,471,472,473,474,475,476,477,478,479,480,481,482,483,484,485,486,487,488,489,490,491,492,493,494,495,496,497,498,499,500,501,502,503,504,505,506,507,508,509,510,561,562,563,564,565,566,567,568,569,570,571,572,573,574,575,576,577,578,579,580,581,582,583,584,585,586,587,588,589,590,591,592,593,594,595,596,597,598,599,600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,621,622,623,624,625,626,627,628,629,630,631,632,633,634,635,636,637,639,640,641,642,643,644,645,646,647,648,649,650,651,652,653,654,655,656,657,658,659,660,661,662,663,664,665,666,667,668,669,670,671,672,673,674,675,676,677,678,679,680,681,682,683,684,685,686,687,688,689,690,691,692,693,694,695,696,697,698,699,700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,718,719,720,721,722,723,724,725,726,727,728,729,730,731,732,733,734,735,736,737,738,739,740,741,742,743,744,745,746,747,748,749,750,752,754,755,756,757,758,759,760,761,762,763,764,765,766,767,768,770,771,772,773,774,775,776,777,778,779,780,781,782,783,784,785,786,787,788,789,790,791,792,793,794,795,796,797,798,799)

  /** Get contents of the cleaned file with ID `i`
    *
    * Possible first lines containing URL: http://... are removed.
    */
  def loadCleanFile(i: Int): String =
    loadCleanFile(cleanPath(i), isResource=true)

  /** Get contents of the HTML source with ID `i` */
  def loadOrigFile(i: Int): String = Util.loadFile(origPath(i), isResource=true)

  /** Get contents of the aligned clean version with ID `i` */
  def loadAlignedFile(i: Int): String = Util.loadFile(alignedPath(i), isResource=true)

  /** Get contents of a cleaned file, stored anywhere
    *
    * Possible first lines containing URL: http://... are removed.
    */
  def loadCleanFile(path: String, normalize: Boolean = false, isResource: Boolean = false): String = {
    val f = Util.loadFile(path, isResource=isResource)

    val contents = if (f.startsWith("URL:")) f.lines.drop(1).mkString("\n")
                   else f

    if (normalize)
      normalizeCleanFile(contents)
    else
      contents
  }

  /** Normalize a cleaned file by: replacing whitespace by a space,
    * removing <H> <P> <L> and list markings from the file
    * and trimming it.
    */
  def normalizeCleanFile(txt: String): String =
    txt.toUpperCase
       .replaceAll("""[\p{Z}\s]+"""," ")
       .replaceAll("""<(P|L|H)>|\* |[0-9]+\. |_+""","")
       .trim
       // .replaceAll("""[^\x00-\x7F]"""," ")
       //.replaceAll("""\W"""," ")

  /** Generate all alignment files and store them in `outputDir`. */
  def generateAlignedFiles(outputDir: String) = {
    indices.foreach { index => {

      val outFile = s"$outputDir/$index.txt"

      println("")
      println(s"###########################")
      println(s"### Aligning $index ...")
      println(s"###########################")

      if (!Util.fileExists(outFile)) {
        val source = loadOrigFile(index)
        val clean  = normalizeCleanFile(loadCleanFile(index))
        val output = Alignment.alignment(source, clean)
        Util.save(outFile,output)
      }

    }}
  }

  /** Create an iterator that produces the cleaneval pages with all available content one by one. */
  def iterator: Iterator[Page] =
    indices.toIterator.map {i => Page(
      id      = i,
      orig    = loadOrigFile(i),
      clean   = loadCleanFile(i),
      aligned = loadAlignedFile(i)
    )}

  /** Generate a dataset for training / testing a classifier
    * @param take How many documents to use (-1 = all) */
  def dataset(extractor: FeatureExtractor, take: Int = -1): Vector[(PageFeatures,Vector[Int])] = {
      val it = if (take == -1) iterator
               else iterator.take(take)
      (it map { (p: Page) => {
        val body = Jsoup.parse(p.origWithoutTextTag).body
        val cdom = CDOM.fromBody(body)
        (extractor(cdom),Alignment.labelsFromAlignedString(cdom, p.aligned))
      }}).toVector
  }

  def evaluateOtherCleaner(alignedLocation: Int=>String): PerformanceStatistics = {

    val pairs = iterator.flatMap( (p: Page) => {

      val location = alignedLocation(p.id)
      if (!Util.fileExists(location)) Vector()
      else {
        // Compute CDOM
        val body = Jsoup.parse(p.origWithoutTextTag).body
        val cdom = CDOM.fromBody(body)

        val goldLabels = Alignment.labelsFromAlignedString(cdom, p.aligned)
        val otherLabels = Alignment.labelsFromAlignedString(cdom, Util.loadFile(location))

        (otherLabels zip goldLabels)
      }
    }).toVector

    PerformanceStatistics.fromPairs(pairs)

  }


}