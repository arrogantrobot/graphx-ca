name := "graphxCellularAutomaton"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % "2.0.0" % "provided",
  "org.apache.spark" % "spark-graphx_2.10" % "2.0.0" % "provided",
  "org.apache.spark" % "spark-sql_2.10" % "2.0.0" % "provided",
  "com.typesafe" % "config" % "1.3.0"
)
