package nl.tvogels.boilerplate.cleaneval

import nl.tvogels.boilerplate.utilities.Util
import nl.tvogels.boilerplate.alignment.Alignment
import nl.tvogels.boilerplate.features.{FeatureExtractor,PageFeatures}
import nl.tvogels.boilerplate.cdom.CDOM
import nl.tvogels.boilerplate.classification.PerformanceStatistics
import nl.tvogels.boilerplate.utilities.Util.codec
import nl.tvogels.boilerplate.database.Database
import scala.io.{Source,Codec}
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


  /** Vector of indices of CleanEval items that are complete with source, cleaned and aligned versions. */
  val indices: Vector[Int] = Vector(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,249,250,251,252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,292,293,294,295,296,297,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,338,339,340,341,342,343,344,345,346,347,348,349,350,351,352,353,354,355,356,357,358,359,360,361,362,363,364,365,366,367,368,369,370,371,372,373,374,375,376,377,378,379,380,381,382,383,384,385,386,387,388,389,390,391,392,393,394,395,396,397,398,399,400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,421,422,424,425,426,427,428,429,430,431,433,434,435,436,437,438,439,440,441,442,443,444,445,446,447,448,449,450,451,452,453,454,455,456,457,458,459,460,461,462,463,464,465,466,467,468,469,470,471,472,473,474,475,476,477,478,479,480,481,482,483,484,485,486,487,488,489,490,491,492,493,494,495,496,497,498,499,500,501,502,503,504,505,506,507,508,509,510,561,562,563,564,565,566,567,568,569,570,571,572,573,574,575,576,577,578,579,580,581,582,583,584,585,586,587,588,589,590,591,592,593,594,595,596,597,598,599,600,601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,621,622,623,624,625,626,627,628,629,630,631,632,633,634,635,636,637,638,639,640,641,642,643,644,645,646,647,648,649,650,651,652,653,654,655,656,657,658,659,660,661,662,663,664,665,666,667,668,669,670,671,672,673,674,675,676,677,678,679,680,681,682,683,684,685,686,687,688,689,690,691,692,693,694,695,696,697,698,699,700,701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,718,719,720,721,722,723,724,725,726,727,728,729,730,731,732,733,734,735,736,737,738,739,740,741,742,743,744,745,746,747,748,749,750,752,754,755,756,757,758,759,760,761,762,763,764,765,766,767,768,770,771,772,773,774,775,776,777,778,779,780,781,782,783,784,785,786,787,788,789,790,791,792,793,794,795,796,797,798,799)

  val testSet: Vector[Int] = Vector(309,87,534,705,362,192,247,538,311,29,494,202,210,636,183,238,628,64
                                   ,126,469,345,429,732,697,235,566,234,108,62,158,484,299,503,459,643,213
                                   ,243,522,656,506,291,50,110,714,406,711,303,435,633,260,701,142,421,240
                                   ,59,187,314,2,136,682,54,602,427,718,157,381,195,127,683,392,5,122
                                   ,93,451,629,83,616,458,542,334,428,596,205,693,91,177,215,252,140,500
                                   ,649,448,443,594,728,266,239,554,570,452,152,323,576,598,473,100,173,275
                                   ,627,525,8,284,26,37,130,472,208,222,352,52,45,367,278,106,615,15
                                   ,432,246,166,721,145,63,608,694,346,600,450,612,441,102,20,658,471,673
                                   ,118,333,437,254)

  val trainingSet: Vector[Int] = Vector(133,614,30,546,317,85,544,115,657,607,231,331,4,555,274,194,310,515
                                       ,230,60,9,56,414,404,300,156,365,606,96,646,281,304,426,149,258,734
                                       ,305,461,569,324,185,90,670,417,604,14,270,125,655,651,422,640,677,446
                                       ,467,236,123,457,736,487,207,592,178,287,285,444,28,165,144,347,27,580
                                       ,384,121,94,558,708,167,526,593,678,476,95,639,552,38,725,716,514,19
                                       ,164,648,168,505,495,563,288,321,295,242,702,332,585,712,139,520,686,204
                                       ,545,181,344,147,359,513,498,101,339,499,329,0,493,726,518,549,11,386
                                       ,294,6,372,18,328,369,727,13,292,556,244,488,76,403,379,206,407,610
                                       ,445,169,480,301,289,129,58,150,695,35,653,201,259,373,191,349,162,376
                                       ,591,605,306,595,584,692,25,172,117,67,401,355,221,541,637,397,430,160
                                       ,336,572,603,690,641,88,613,492,709,509,405,225,416,632,176,685,375,48
                                       ,277,65,562,577,154,523,33,589,391,81,319,105,297,394,667,665,507,357
                                       ,650,453,353,691,400,214,174,31,47,330,224,533,620,276,280,590,312,198
                                       ,696,631,92,460,521,290,618,389,537,623,146,699,364,229,418,465,170,86
                                       ,283,113,642,666,84,663,424,578,107,587,197,661,536,73,659,468,687,219
                                       ,609,704,44,466,89,436,551,486,322,335,218,241,116,57,21,23,581,226
                                       ,635,212,159,710,217,647,24,228,134,361,733,388,70,1,599,722,97,724
                                       ,720,684,502,396,703,313,385,82,272,634,175,196,77,393,398,539,611,630
                                       ,17,621,413,104,261,586,12,69,80,220,454,689,588,654,232,250,652,315
                                       ,491,343,617,524,7,497,719,735,98,103,293,327,540,378,688,531,489,141
                                       ,265,132,624,660,481,255,438,671,216,561,61,464,43,51,574,383,325,463
                                       ,439,271,99,567,597,644,700,302,535,490,143,390,40,431,456,559,530,66
                                       ,120,279,434,326,411,74,55,423,449,504,32,568,227,184,462,138,153,209
                                       ,253,233,571,245,203,223,257,420,583,36,676,39,474,263,717,433,625,706
                                       ,707,148,273,399,22,10,49,512,366,249,675,478,199,79,356,517,237,669
                                       ,511,71,395,519,41,189,42,715,46,342,662,308,668,547,358,351,412,485
                                       ,713,128,296,529,626,151,182,508,251,527,155,565,543,455,573,348,638,560
                                       ,282,442,674,68,211,109,601,557,368,510,387,370,409,186,337,419,268,622
                                       ,408,114,382,550,137,363,350,135,679,619,582,34,340,286,723,163,380,470
                                       ,111,410,360,528,256,482,53,731,553,180,264,338,477,664,119,501,672,72
                                       ,425,341,564,496,440,262,131,307,374,269,248,681,377,729,698,190,579,447
                                       ,475,354,680,402,532,200,575,267,318,548,3,161,320,415,479,171,371,16
                                       ,316,483,645,298,179,75,730,124,78,188,112,193,516)

  /** Get contents of the cleaned file with ID `i`
    *
    * Possible first lines containing URL: http://... are removed.
    */
  def loadCleanFile(i: Int): String =
    loadCleanFile(cleanPath(i), isResource=true, normalize=true)

  /** Get contents of the HTML source with ID `i` */
  def loadOrigFile(id: Int): String = {
    val path = origPath(id)
    val stream = getClass.getResourceAsStream(path)
    val source = Source.fromInputStream(stream)
    val lines = source.getLines.drop(1).toVector
    lines.slice(0,lines.length-1) mkString "\n"
  }

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

  def loadFirstLine(id: Int) = {
    val path = CleanEval.origPath(id)
    val stream = getClass.getResourceAsStream(path)
    val source = Source.fromInputStream(stream)
    source.getLines.next()
  }

  /** Normalize a cleaned file by removing <H> <P> <L> and list markings from the file
    * and trimming it.
    */
  def normalizeCleanFile(txt: String): String =
    txt
       .replaceAll("""(?i)(?m)^\s*<l>\s*(»|\*|\d{1,2}\.\s)\s*|<(l|h|p)>\s*|^\s*(_{10,}|-{10,})\s*$|^\s*""","")
       .trim

  /** Generate all alignment files and store them in `outputDir`. */
  def generateAlignedFiles(outputDir: String) = {
    iterator.foreach { p => {

      val outFile = s"$outputDir/${p.id}.txt"

      println(s"Aligning ${p.id} ...")

      if (!Util.fileExists(outFile)) {
        val output = Alignment.alignment(p.source, p.clean)
        assert(
          output.length == p.source.length,
          "Aligned file does not have the same length as the source."
        )
        Util.save(outFile,output)
      }

    }}
  }

  /** Add CleanEval data to the Mongo database */
  def addToMongo(db: Database): Unit = {
    val datasetId = "cleaneval"

    db.createIndices
    db.insertDataset(datasetId, "CleanEval 2007")

    for (page <- iterator) {
      println(s"Working on document ${page.id} ...")

      db.insertDocument(
        dataset  = datasetId,
        docId    = page.docId,
        source   = page.source,
        url      = page.url,
        encoding = page.encoding
      )

      val groundtruth = Alignment.extractLabels(CDOM(page.source), page.aligned)
      val metadata = Map("clean" -> page.clean, "aligned" -> page.aligned)
      db.insertLabels(
        docId         = page.docId,
        dataset       = datasetId,
        labelName     = "ground_truth",
        labels        = groundtruth,
        userGenerated = false,
        metadata      = metadata
      )
    }
  }


  /** Create an iterator that produces the cleaneval pages with all available content one by one. */
  def iterator: Iterator[Page] =
    indices.toIterator.map {i => Page(i) }

  def trainingIterator: Iterator[Page] =
    trainingSet.toIterator.map {i => Page(i) }

  def testIterator: Iterator[Page] =
    testSet.toIterator.map {i => Page(i) }


  /** Generate a dataset for training / testing a classifier
    * @param take How many documents to use (-1 = all) */
  def dataset(extractor: FeatureExtractor, take: Int = -1): Vector[(PageFeatures,Vector[Int])] = {

      val it = if (take == -1) iterator else iterator.take(take)

      val result = it map { p =>
        val cdom = CDOM(p.source)
        val features = extractor(cdom)
        val labels = Alignment.extractLabels(cdom, p.aligned)
        (features,labels)
      }

      result.toVector
  }

  /** Generate a dataset for training / testing a classifier */
  def trainingDataset(extractor: FeatureExtractor): Vector[(PageFeatures,Vector[Int])] = {

      val it = trainingIterator

      val result = it map { p =>
        val cdom = CDOM(p.source)
        val features = extractor(cdom)
        val labels = Alignment.extractLabels(cdom, p.aligned)
        (features,labels)
      }

      result.toVector
  }

  /** Generate a dataset for training / testing a classifier */
  def testDataset(extractor: FeatureExtractor): Vector[(PageFeatures,Vector[Int])] = {

      val it = testIterator

      val result = it map { p =>
        val cdom = CDOM(p.source)
        val features = extractor(cdom)
        val labels = Alignment.extractLabels(cdom, p.aligned)
        (features,labels)
      }

      result.toVector
  }

  /** Evaluate a cleaner by its aligned files.
    * If a cleaned file for a certain index is missing, it is skipped without penalty.
    * @param alignedLocation function turning an index to the file location. */
  def evaluateCleaner(alignedLocation: Int=>String): PerformanceStatistics = {

    val pairs = testIterator flatMap { p =>

      val path = alignedLocation(p.id)

      if (!Util.fileExists(path)) {
        Vector()
      } else {
        import Alignment.extractLabels
        val cdom        = CDOM(p.source)
        val goldLabels  = extractLabels(cdom, p.aligned)
        val otherLabels = extractLabels(cdom, Util.loadFile(path))
        (otherLabels zip goldLabels)
      }
    }
    PerformanceStatistics.fromPairs(pairs.toVector)
  }


}