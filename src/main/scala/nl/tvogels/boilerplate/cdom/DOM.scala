package nl.tvogels.boilerplate.cdom

import org.jsoup.{nodes => jnodes}
import nl.tvogels.boilerplate.utilities.Settings
import scala.collection.JavaConversions._


/** Collection of miscellaneous tools related to the Jsoup DOM */
object DOM {

  /** Pattern matching class for empty nodes */
  object EmptyNode {
    def unapply(z: jnodes.Element): Option[jnodes.Element] =
      if (z.text matches """^[\p{Z}\s]*$""") Some(z) else None
  }

  /** Pattern matching class for empty text nodes */
  object EmptyTextNode {
    def unapply(z: jnodes.TextNode): Option[jnodes.TextNode] =
      if (z.text matches """^[\p{Z}\s]*$""") Some(z) else None
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

}