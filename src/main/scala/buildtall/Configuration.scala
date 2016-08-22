package buildtall

import com.typesafe.config.ConfigFactory

object Configuration {
  val conf = ConfigFactory.load()

  val adjacencyList = conf.getString("input.adjacency_list.path")
  val message = conf.getString("message")

  val numCells = conf.getLong("numCells")
}
