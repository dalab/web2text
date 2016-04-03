package nl.tvogels.boilerplate.cdom

/** A CDOM Node, including node level features
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
class Node {

  /** A list of node tags from top to bottom: `["ul","li","a"]` */
  var tags: Vector[String]       = Vector()

  /** A list of classNames from top to bottom */
  var classNames: Vector[Set[String]] = Vector()

  /** A list of node attributes from top to bottom */
  var attributes: Vector[java.util.Map[String,String]] = Vector()

  /** Pointer to the parent node, if any */
  var parent: Option[Node]    = None

  /** Pointers to the children, in a vector. A leaf has Vector(). */
  var children: Vector[Node]  = Vector()

  /** Pointer to the left sibbling, if any */
  var lsibbling: Option[Node] = None

  /** Pointer to the right sibbling, if any */
  var rsibbling: Option[Node] = None

  /** Node text: only relevant for text nodes / leaves */
  var text: String            = ""

  /** Node properties */
  var properties: NodeProperties  = null

  /** To string function */
  override def toString = {
    val tagStr = (tags mkString "/")
    val c = for (c <- children; l <- c.toString.lines) yield {"  " + l}
    (tagStr :: c.toList).mkString("\n")
  }

  /** Class to be able to do nice pattern matching on Leaves */
  object Leaf {
    def unapply(z: Node): Option[Node] =
      if (z.children.length == 0) Some(z) else None
  }

  /** Find all leaves under the current CDOM node */
  def leaves: Vector[Node] = this match {
    case Leaf(x) => Vector(x)
    case _ => children.flatMap(_.leaves)
  }

  /** Generate an <ul> for export to HTML */
  def treeHTML: String = {
    val main = s"<a href='#'>${tags.filter(x => x!="#text") mkString " / "}<div class='features'>${properties.toHTML}</div></a>"
    val childStuff = (if (children.length == 0) ""
                    else "<ul>"+children.map(c => s"<li>${c.treeHTML}</li>").mkString("\n")+"</ul>")
    main + "\n" + childStuff
  }

  def ancestors: Stream[Node] = parent match {
    case None         => Stream.empty
    case Some(parent) => parent #:: parent.ancestors
  }

  def tagPlusClass: String = {
    (tags zip classNames) map {
      case (tag, cn) => {
        val classString = (cn map { c => s".$c"}).mkString
        tag + classString
      }
    } mkString ","
  }

  def classSelector: String = {
    (s"[${tagPlusClass}]" #:: (ancestors map { a =>
      s"[${a.tagPlusClass}]"
    })) mkString " "
  }
}