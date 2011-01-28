package improving
package attach
package cmd

object Show {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      return println("Usage: show [pid1 pid2 ...]")
  
    for (arg <- args ; bean <- VM(arg).mbeans)
      println(bean)
  }
}
