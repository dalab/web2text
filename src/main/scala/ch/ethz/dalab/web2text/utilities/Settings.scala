package ch.ethz.dalab.web2text.utilities

/** Miscellaneous settings for the project
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
object Settings {

  /** Collection of English stopwords */
  val stopwords = Set("a","about","above","after","again","against","all","am","an","and","any","are","as","at","be","because","been","before","being","below","between","both","but","by","cannot","could","did","do","does","doing","down","during","each","few","for","from","further","had","has","have","having","he","her","here","hers","herself","him","himself","his","how","i","if","in","into","is","it","its","itself","me","more","most","my","myself","no","nor","not","of","off","on","once","only","or","other","ought","our","ours", "ourselves","out","over","own","same","she","should","so","some","such","than","that","the","their","theirs","them","themselves","then","there","these","they","this","those","through","to","too","under","until","up","very","was","we","were","what","when","where","which","while","who","whom","why","with","would","you","your","yours","yourself","yourselves")

  /** List of characters that are considered punctuation */
  val punctuation = Set('.',',','?',';',':','!')

  /** List of dashes */
  val dashes = Set('-','_','/','\\')

  /** Tags not to be incorporated for the CDOM */
  val skipTags = Set("#doctype", "br", "checkbox", "head", "hr", "iframe", "img", "input", "meta", "noscript", "radio", "script", "select", "style", "textarea", "title", "video")

  /** Block level HTML elements */
  val blockTags = Set("address", "article", "aside", "blockquote", "body", "canvas",
                        "center", "checkbox", "dd", "div", "dl", "fieldset", "figcaption",
                        "figure", "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6",
                        "head", "header", "hgroup", "hr", "html", "iframe", "input", "li",
                        "main", "nav", "noscript", "ol", "ol", "output", "p", "pre", "radio",
                        "section", "select", "table", "tbody", "td", "textarea", "tfoot",
                        "thead", "tr", "ul", "video","br")

  /** Collection of interesting regular expressions */
  object regex {
    // Regular expressions from Mozilla Readability (https://github.com/mozilla/readability/blob/master/Readability.js)
    val unlikelyCandidates = """(?i)banner|combx|comment|community|disqus|extra|foot|header|menu|related|remark|rss|sh|are|shoutbox|sidebar|skyscraper|sponsor|ad-break|agegate|pagination|pager|popup""".r
    val okMaybeItsACandidate = """(?i)and|article|body|column|main|shadow""".r
    val positive = """(?i)article|body|content|entry|hentry|main|page|pagination|post|text|blog|story""".r
    val negative = """(?i)hidden|banner|combx|comment|com-|contact|foot|footer|footnote|masthead|media|meta|outbrain|promo|related|scroll|share|shoutbox|sidebar|skyscraper|sponsor|shopping|tags|tool|widget""".r
    val extraneous = """(?i)print|archive|comment|discuss|e[\-]?mail|share|reply|all|login|sign|single|utility""".r
    val byline = """(?i)byline|author|dateline|writtenby""".r
  }

}
