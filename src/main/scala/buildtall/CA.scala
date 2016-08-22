package buildtall

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.graphx._

case class Neighborhood( left:Boolean, mid:Boolean, right:Boolean){
  override def toString: String = {
    (if(left) "1" else "0") + (if(mid) "1" else "0") + (if(right) "1" else "0")
  }
}

object Neighborhood {
  //def apply(left: Boolean, mid:Boolean, right:Boolean):Neighborhood = new Neighborhood(left, mid, right)
  def combine(n1: Neighborhood, n2:Neighborhood):Neighborhood = {
    Neighborhood( n1.left || n2.left, n1.mid || n2.mid, n1.right || n2.right)
  }

  val twos = Array(1, 2, 4, 8, 16, 32, 64, 128)

  def applyRule(n:Neighborhood, rule:Int):Neighborhood = {
    val num = (if (n.left) 1 else 0) + (if (n.mid) 2 else 0) + (if(n.right) 4 else 0)
    val t = twos(num)
    Neighborhood(false, (t & rule) > 0 , false)
  }
}

object CA {

  type CAState = Neighborhood
  type VD = (VertexId,CAState)
  type ED = String

  def initVertices(id:VertexId):VD = {
    if (id == Configuration.numCells/2) (id, Neighborhood(false, true, false))
    else { (id, Neighborhood(false, false, false))}
  }

  def getRightId(id:VertexId):VertexId = {
    if(id + 1 >= Configuration.numCells) 0L
    else id + 1
  }

  def getLeftId(id:VertexId):VertexId = {
    if(id - 1 < 0) Configuration.numCells - 1
    else id - 1
  }

  def initEdges(v:VD):Seq[Edge[ED]] = {
    Seq(
      Edge(v._1, getRightId(v._1), "isRight"),
      Edge(v._1, getLeftId(v._1), "isLeft")
    )
  }

  def iterate(g:Graph[CAState, ED], rule:Int):Graph[CAState,ED] = {
    val vs = g.aggregateMessages[Neighborhood](triplet => {
      val mid = triplet.srcAttr.mid
      val n = if (triplet.toEdgeTriplet.attr == "isLeft") Neighborhood(false, mid, triplet.dstAttr.mid)
      else Neighborhood(triplet.dstAttr.mid, mid, false)
      triplet.sendToSrc(n)
    },
      (a,b) => Neighborhood.combine(a,b)
    ).map(v => {
      (v._1, v._2)
    })

    val v = VertexRDD(vs.map(verts => (verts._1, Neighborhood.applyRule(verts._2, rule))))
    showStates(v)
    Graph(v, g.edges)
  }

  def showStates(v:VertexRDD[Neighborhood]):String = {
    v.collect().sortBy(_._1).map(x => if (x._2.mid) "1" else "0").mkString
  }

  def main(args: Array[String]):Unit = {
    val conf = new SparkConf().setAppName("GraphX Cellular Automata")
    val sc = new SparkContext(conf)

    val vrdd = VertexRDD(sc.parallelize(0L until Configuration.numCells).map(initVertices))
    val edges = vrdd.flatMap(initEdges)


    val g = Graph(vrdd, edges)

    g.persist()

    var answer = List[String](showStates(g.vertices))

    val iterations = 100
    var count = 0

    var graph = g
    while(count < iterations) {
      graph = iterate(graph, 18)
      //graph.vertices.collect().sortBy(x => x._1)foreach(v => println(s"${v._1}===================================================="))
      answer = answer :+ showStates(graph.vertices)
      count += 1
    }
    answer.foreach(println)

    //graph.edges.foreach(e => println(s"src: ${e.srcId} dest: ${e.dstId} prop: ${e.attr}"))
  }
}