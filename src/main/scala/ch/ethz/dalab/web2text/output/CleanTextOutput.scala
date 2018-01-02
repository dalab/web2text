package ch.ethz.dalab.web2text.output

import ch.ethz.dalab.web2text.cdom.CDOM

object CleanTextOutput {

  def apply(cdom: CDOM, labels: Seq[Int]): String = {
    var outstring = ""
    var breaked = true
    cdom.leaves.zip(labels).foreach {
      case (leaf, 1) => {
        breaked = false
        outstring += leaf.text
        if(leaf.properties.blockBreakAfter) {
          outstring += "\n\n"
          breaked = true
        }
      }
      case (leaf, 0) => {
        if(leaf.properties.blockBreakAfter && !breaked) {
          outstring += "\n\n"
          breaked = true
        }
      }
    }

    return outstring
  }

}
