name := "Boilerplate"

organization := "nl.tvogels"

version:= "2.0-SNAPSHOT"

scalaVersion := "2.10.4"

cancelable in Global := true
fork in run := true

libraryDependencies += "ch.ethz.dalab" %% "dissolvestruct" % "0.1-SNAPSHOT"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.2.4" % "test"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.4.1"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.4.1"

libraryDependencies += "org.mongodb" %% "casbah" % "3.1.0"