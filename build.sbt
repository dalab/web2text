name := "Boilerplate"

organization := "nl.tvogels"

version:= "2.0"

scalaVersion := "2.10.4"


libraryDependencies += "ch.ethz.dalab" %% "dissolvestruct" % "0.1-SNAPSHOT"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.2.4" % "test"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.2"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.2"