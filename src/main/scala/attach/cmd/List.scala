package improving
package attach
package cmd

object List {
  def main(args: Array[String]): Unit = {
    val pairs = vmMap.toList sortBy (_._1)
    pairs foreach { case (k, v) => println("%10s: %s".format(k, v)) } 
  }
}
