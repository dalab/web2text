package nl.tvogels.boilerplate.features.extractor

import nl.tvogels.boilerplate.cdom.{Node,CDOM}
import nl.tvogels.boilerplate.features.BlockFeatureExtractor

/** Block extractor for tag features */
object TagExtractor extends BlockFeatureExtractor {

  /** Set of all tags */
  private val allTags = Vector("body","address","article","aside","blockquote","dd","div","dl","fieldset","figcaption","figure","figcaption","footer","form","h1","h2","h3","h4","h5","h6","header","hgroup","li","main","nav","noscript","ol","output","p","pre","section","table","tfoot","ul") ++
    Vector("b","big","i","small","tt","abbr","acronym","cite","code","dfn","em","kbd","strong","samp","time","var","a","bdo","q","span","sub","sup","label")++
     Vector("td","tr","th","thead","tbody")

  /** Apply the function */
  def apply(cdom: CDOM) = (node: Node) =>
    allTags.map{ case t => if (node.tags.contains(t)) 1.0 else 0.0 }

  val labels = allTags.map { tag => s"tag_$tag" }

}