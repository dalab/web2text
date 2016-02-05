package nl.tvogels.boilerplate.utilities

import scala.annotation.tailrec
import scala.io.Source
import java.nio.file.{Paths, Files}
import scala.util.Random
import java.io.FileNotFoundException

import java.nio.charset.CodingErrorAction
import scala.io.Codec

/** Miscelaneous utility functions
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
object Util {

  /** Codec configured for loading non-UTF8 files */
  private implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  /** Find the K-Median of a list of numbers
    *
    * @param arr An array of Numbers
    * @param k Search for k'th smallest number
    * @return k'th smallest number
    *
    * @todo Make this general for any Numeric type
    *
    * @note Based on an answer from StackOverflow
    */
  @tailrec def findKMedian(arr: Seq[Double], k: Int): Double = {
    val a = arr(scala.util.Random.nextInt(arr.size))
    val (s, b) = arr partition (a >)
    if (s.size == k) a
    // The following test is used to avoid infinite repetition
    else if (s.isEmpty) {
      val (s, b) = arr partition (a ==)
      if (s.size > k) a
      else findKMedian(b, k - s.size)
    } else if (s.size < k) findKMedian(b, k - s.size)
    else findKMedian(s, k)
  }

  /** Find the median in a list */
  def median(arr: Seq[Double]) = findKMedian(arr, (arr.size - 1) / 2)

  /** Time the execution of a block
    *
    * Prints the elapsed time
    */
  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0).toDouble / 1000000000 + "s")
    result
  }

  /** Finds all occurrences of a string `needle` in `haystack`
    *
    * @return A list of positions of the occurences
    */
  def allSubstringOccurences(haystack: String, needle: String): List[Int] = {
    @tailrec def count(pos: Int, c: List[Int]): List[Int] = {
      val idx = haystack indexOf (needle, pos)
      if (idx == -1) c else count(idx + needle.size, idx :: c)
    }
    count(0, List())
  }

  /** Finds all occurrences of a string `needle` in `haystack` between bounds
    *
    * @param haystack
    * @param needle
    * @param searchFrom Find matches starting at index
    * @param searchTo Stop finding matches at index
    * @return A list of positions of the occurences
    */
  def allSubstringOccurences(haystack: String, needle: String, searchFrom: Int, searchTo: Int): List[Int] = {
    @tailrec def count(pos: Int, c: List[Int]): List[Int] = {
      val idx = haystack indexOf (needle, pos)
      if (idx == -1 || idx > searchTo) c else count(idx + needle.size, idx :: c)
    }
    count(searchFrom, List())
  }

  /** Load a file from disk
    *
    * @param path File path
    * @param skipLines Skip the first `skipLines` lines
    * @param isResource Boolean to specify whether to look in /src/main/resources/,
    * or just at the path provided
    */
  def loadFile(path: String, skipLines: Int = 0, isResource: Boolean = false) = {

    val source = {
      if (isResource) {
        val is = getClass.getResourceAsStream(path)
        Source.fromInputStream(is)
      }
      else Source.fromFile(path)
    }

    source.getLines.drop(skipLines) mkString "\n"
  }

  /** Randomly select an element from a sequence, with given probabilities
    *
    * @param elements List of elements, of which one will be picked
    * @param weights Picking probabilities. Does not have to a valid PDF
    * @return Selected element
    */
  def randomSelectionWeighted[X](elements: Seq[X], weights: Seq[Double]): X = {
    assert(weights.length == elements.length)
    def normalize(l: Seq[Double]) = {
      val sum = l.sum
      if (sum == 0d) l.map(x => x / l.length) else l.map(x => x / sum)
    }
    val nweights = normalize(weights)
    val cum_ = nweights.scan(0d)(_ + _).tail
    val cum = cum_.updated(cum_.length - 1, 1d)
    val rand = Random.nextDouble
    elements((cum.indexWhere { x => x > rand } - 1).max(0))
  }

  /** Show the first `n` characters of a string `s`, if the string is longer */
  def preview(s: String, n: Int) =
    if (s.length <= n) s
    else s.take(s.lastIndexWhere(_.isSpaceChar, n)).trim

  /** Save a file to disk */
  def save(filename: String, content: String): Unit = {
    val file = new java.io.File(filename)
    val out = new java.io.BufferedWriter(new java.io.FileWriter(file))
    out.write(content)
    out.close
  }

  /** Check if a file exists */
  def fileExists(path: String) =
    Files.exists(Paths.get(path))

  /** Trim any whitespace from the ends of a string */
  def trim(s: String): String = s.replaceAll("(^\\h*)|(\\h*$)", "")

}