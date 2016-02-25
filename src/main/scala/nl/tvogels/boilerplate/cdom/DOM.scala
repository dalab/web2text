package nl.tvogels.boilerplate.cdom

import org.jsoup.{nodes => jnodes}
import nl.tvogels.boilerplate.utilities.Settings
import scala.collection.JavaConversions._


/** Collection of miscellaneous tools related to the Jsoup DOM */
object DOM {

  /** Class for text blocks, when inserted by the function wrapBlocks */
  val BLOCK_CLASS = "boilerplate-text-block"

  /** Pattern matching class for empty nodes */
  object EmptyNode {
    def unapply(z: jnodes.Element): Option[jnodes.Element] =
      if (z.text.trim matches """^[\p{Z}\s]*$""") Some(z) else None
  }


  /** Pattern matching class for empty text nodes */
  object EmptyTextNode {
    def unapply(z: jnodes.TextNode): Option[jnodes.TextNode] =
      if (z.text.trim matches """^[\p{Z}\s]*$""") Some(z) else None
  }

  /** Pattern matching class for nodes that are not to be incorporated in the CDOM */
  object SkipNode {
    def unapply(z: jnodes.Node): Option[jnodes.Node] = {
      if (Settings.skipTags.contains(z.nodeName)) Some(z) else None
    }
  }

  /** Pattern maching class for block level element that have no block level children */
  object BlockLeaf {

    def hasBlockLevelChildren(z: jnodes.Element): Boolean = {
      z.getAllElements.tail.find(x => Settings.blockTags.contains(x.nodeName)) != None
    }

    def unapply(z: jnodes.Element): Option[jnodes.Element] = {
      if (!hasBlockLevelChildren(z)) Some(z) else None
    }
  }

  /** Extract the text from a node, and return "" if not supported */
  def text(node: jnodes.Node) = node match {
    case n: jnodes.TextNode => n.text
    case n: jnodes.Element => n.text
    case _ => ""
  }

  /** Wraps blocks in <span class="boilerplate-text-block"/> */
  def wrapBlocks(dom: jnodes.Element): Unit = {
    var id = 0
    def wrapTextNodes(node: jnodes.Node): Unit = {
      node match {
        case DOM.EmptyNode(x) => None
        case DOM.EmptyTextNode(x) => None
        case DOM.SkipNode(x)  => None
        case leaf: jnodes.TextNode  => {
          leaf.wrap(s"""<span class="$BLOCK_CLASS" data-id="$id"></span>""")
          id += 1
        }
        case branch => branch.childNodes.map(wrapTextNodes)
      }
    }

    wrapTextNodes(dom)
  }

  /** Makes the DOM safe for an annotation tool, by removing interactivity
    * and links. */
  def removeJavascript(dom: jnodes.Element): Unit = {
    dom.select("script").map(_.remove)
    dom.select("noscript").map(_.remove)
    dom.select("[onclick]").map(n => n.attr("onclick",""))
    dom.select("[onchange]").map(n => n.attr("onchange",""))
    dom.select("[onmouseup]").map(n => n.attr("onmouseup",""))
    dom.select("[onmousedown]").map(n => n.attr("onmousedown",""))
    dom.select("[onload]").map(n => n.attr("onload",""))
    dom.select("meta[http-equiv=\"refresh\"]").map(_.remove)
    dom.select("iframe").map(_.remove)
  }

  def inactivateLinks(dom: jnodes.Element): Unit = {
    dom.select("a[href]").map(n => n.attr("href",""))
  }

  def makeUrlsAbsolute(dom: jnodes.Element): Unit = {
    dom.select("[src]").map(n => n.attr("src",n.attr("abs:src")));
    dom.select("link[href]").map(n => n.attr("href",n.attr("abs:href")));
  }

}