package ch.ethz.dalab.web2text.cdom

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jsoup.{nodes => jnodes}
import org.jsoup.Jsoup
import ch.ethz.dalab.web2text.utilities.Util
import scala.collection.mutable

/** CDOM
  *
  * @author Thijs Vogels <t.vogels@me.com>
  * @todo Add more documentation.
  */
class CDOM(val root: Node, val leaves: Vector[Node]) {

  /** Generate an ASCII tree of the CDOM */
  override def toString: String = root.toString

  /** Generate an HTML file that visually shows the CDOM tree */
  def saveHTML(path: String) = {
    val css = Util.loadFile("/cdom_treeview.css", isResource=true)
    val tree = root.treeHTML
    val html = s"""<!doctype html>
                  |<html lang='en'>
                  |  <head>
                  |    <meta charset='utf8'>
                  |    <title>CDOM Tree</title>
                  |    <style>$css</style>
                  |  </head>
                  |  <body>
                  |    <div class='tree'><ul><li>$tree</li></ul></div>
                  |  </body>
                  |</html>""".stripMargin
    Util.save(path, html)
  }

  lazy val textCounts: mutable.Map[String,Int] = {
    val map = new mutable.HashMap[String,Int].withDefaultValue(0)
    leaves foreach { l => map(l.text) += 1 }
    map
  }
  lazy val classSelectorCounts: mutable.Map[String,Int] = {
    val map = new mutable.HashMap[String,Int].withDefaultValue(0)
    leaves foreach { l => map(l.classSelector) += 1 }
    map
  }

}

/** Factory for CDOM
  * @todo store a list of leave pointers
  */
object CDOM {

  /** Create a CDOM from body text (by parsing it with Jsoup first) */
  def fromHTML(html: String): CDOM = fromBody(Jsoup.parse(html).body)

  /** Create a CDOM from body text (by parsing it with Jsoup first) */
  def apply(html: String): CDOM = fromHTML(html)

  /** Create a CDOM from a DOM node (probably the body of the page) */
  def apply(body: jnodes.Node): CDOM = fromBody(body)

  /** Create a CDOM from a DOM node (probably the body of the page) */
  def fromBody(domnode: jnodes.Node): CDOM = {

    /** Create the CDOM Node for a DOM Node. Returns None if the node has no children,
      * or if the node should be discarded (If it is empty or skipped) */
    def createNodeWithChildrenAndFeatures(domnode: jnodes.Node): Option[Node] =
    domnode match {

      case DOM.EmptyNode(x) => None
      case DOM.EmptyTextNode(x) => None
      case DOM.SkipNode(x)  => None

      // In case of a text node, generate a leaf
      case domnode: jnodes.TextNode  => {

        var node = new Node

        node.tags       = Vector(domnode.nodeName)
        node.classNames = Vector(Set())
        node.attributes = Vector(domnode.attributes.dataset())
        node.text       = domnode.text
        node.properties   = NodeProperties.fromNode(domnode, children=Vector())

        Some(node)
      }

      case domnode: jnodes.Element => {
        // Filter the children
        val node = new Node

        node.tags       = Vector(domnode.nodeName)
        node.classNames = Vector(domnode.classNames().asScala.toSet)
        node.attributes = Vector(domnode.attributes.dataset())
        node.children   = domnode.childNodes.toVector
                            .map(createNodeWithChildrenAndFeatures)
                            .flatten


        node.children.length match {
          // Discard a non-text node if there are no children
          case 0 => None
          case 1 => {
            // There is only one proper child. Do a merge step
            val child = node.children.head
            node.children = child.children
            node.tags = node.tags ++ child.tags
            node.classNames = node.classNames ++ child.classNames
            node.attributes = node.attributes ++ child.attributes
            node.text = child.text
            setPointers(node.children, parent=Some(node))
            node.properties = NodeProperties.fromNode(domnode, children=Vector(child))
            Some(node)
          }
          case _ => {
            // There are multiple children, normal procedure
            node.properties = NodeProperties.fromNode(domnode, children=node.children)
            setPointers(node.children, parent=Some(node))
            Some(node)
          }
        }
      }

      case _  => None

    }

    /** Sets sibbling and parent pointers in a sorted list of sibbling Nodes */
    def setPointers(childSequence: Vector[Node], parent: Option[Node]) = {
      var prev: Option[Node] = None
      childSequence.foreach(x => {
        x.parent = parent
        x.lsibbling = prev
        prev.map(p => p.rsibbling = Some(x))
        prev = Some(x)
      })
    }

    val root = createNodeWithChildrenAndFeatures(domnode).get
    new CDOM(root, root.leaves)
  }

}

