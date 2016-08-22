#!/bin/bash

#sbt package
#${SPARK_HOME}/bin/spark-submit \
#  --class "buildtall.CA" \
#  --master local[4] \
#  target/scala-2.11/graphxcellularautomaton_2.11-0.1.0-SNAPSHOT.jar

sbt assembly
${SPARK_HOME}/bin/spark-submit \
  --class "buildtall.CA" \
  --master local[4] \
  target/scala-2.10/graphxCellularAutomaton-assembly-0.1.0-SNAPSHOT.jar
