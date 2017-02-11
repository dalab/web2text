package nl.tvogels.boilerplate.output

import java.io.{File,PrintWriter}
import nl.tvogels.boilerplate.features.PageFeatures
import breeze.linalg.{DenseMatrix,csvwrite,DenseVector}

/** CSV Writer
  *
  * @author Thijs Vogels <t.vogels@me.com>
  */
object CsvDatasetWriter {

  /**
   * Writes features (block and edge to a directory)
   * The directory will contain two files: block_features.csv and edge_eatures.csv
   */
  def write(data: Seq[(PageFeatures,Vector[Int])], dirPath: String): Unit = {

    val dir = new File(dirPath)
    dir.mkdirs()

    val blockLabels = data.head._1.blockFeatureLabels
    val edgeLabels  = data.head._1.edgeFeatureLabels

    if (blockLabels.length > 0) {

      val rows = for ( ((features, labels), i) <- data.zipWithIndex;
                       (l,j) <- labels.zipWithIndex;
                       row = features.blockFeatures(::,j) )
                 yield (Array(i.toDouble, l.toDouble) ++ row.toArray)

      csvwrite(
        new File(dir, "block_features.csv"),
        new DenseMatrix(rows.head.length, rows.length, rows.flatten.toArray).t
      )
    }
    if (edgeLabels.length > 0) {

      val rows = for ( ((features, labels), i) <- data.zipWithIndex;
                       ((l1,l2),j) <- (labels zip labels.tail).zipWithIndex;
                       row = features.edgeFeatures(::,j) )
                 yield (Array(i.toDouble, (l1*10+l2).toDouble) ++ row.toArray)

      csvwrite(
        new File(dir, "edge_features.csv"),
        new DenseMatrix(rows.head.length, rows.length, rows.flatten.toArray).t
      )
    }
    writeRscript(dir,
                 if (blockLabels.length > 0) Some(blockLabels) else None,
                 if (edgeLabels.length > 0)  Some(edgeLabels)  else None )
  }

  /**
   * Display import code for R
   */
  def writeRscript(dir: File,
                   blockLabels: Option[Seq[String]],
                   edgeLabels: Option[Seq[String]]): Unit = {
    val pw = new PrintWriter(new File(dir, "load.R" ))
    pw.write(s"setwd('${dir.getAbsolutePath()}')\n")
    blockLabels foreach { labels =>
      val str = s"""blocks <- read.csv("block_features.csv", header=F, col.names=c('doc_id','label',${labels map { x => s"'$x'" } mkString ","}))
                   |blocks$$label = as.factor(blocks$$label)\n""".stripMargin
      pw.write(str)
    }
    edgeLabels foreach { labels =>
      val str = s"""edges <- read.csv("edge_features.csv", header=F, col.names=c('doc_id','label',${labels map { x => s"'$x'" } mkString ","}))
                   |edges$$label = factor(edges$$label, levels=c(0,1,10,11), labels=c("00","01","10","11"))\n""".stripMargin
      pw.write(str)
    }
    pw.close()
  }

}