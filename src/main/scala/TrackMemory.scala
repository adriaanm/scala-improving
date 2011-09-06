/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 *
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */
package improving
package memory

import java.lang.management._
import ManagementFactory.getMemoryPoolMXBeans
import scala.collection.JavaConverters._
import scala.tools.util.Signallable
import scala.tools.nsc.io.timer

// +----------------------------------------------+
// +////////////////           |                  +
// +////////////////           |                  +
// +----------------------------------------------+
//
// |--------|
//    init
// |---------------|
//        used
// |---------------------------|
//           committed
// |----------------------------------------------|
//                     max
case class WMemoryUsage(init: Long, used: Long, committed: Long, max: Long) {
  def +(x: WMemoryUsage): WMemoryUsage = WMemoryUsage(init + x.init, used + x.used, committed + x.committed, max + x.max)
  private def m(nanos: Long) = "%.3f Mb" format (nanos / 1000000d)

  override def toString =
   """|    Initial: %s
      |       Used: %s
      |  Committed: %s
      |        Max: %s
      |""".stripMargin.format(m(init), m(used), m(committed), m(max))
}

// Possible arguments are period (in seconds) and signal.
class TrackMemory(argMap: Map[String, String]) {
  val periodic  = argMap get "period" collect { case x if x forall (_.isDigit) => x.toInt }
  val signal    = argMap get "signal"
  val startTime = System.nanoTime

  scala.sys addShutdownHook showCurrentUsage()

  signal foreach { s =>
    println("Printing memory stats upon receiving signal " + s + ".")
    Signallable(s, "Show memory utilization statistics.")(showCurrentUsage())
  }
  periodic foreach { s =>
    println("Printing memory stats every " + s + " seconds.")
    setTimer(s)
  }

  def setTimer(secs: Int): Unit =
    timer(secs)({ showCurrentUsage() ; setTimer(secs) })

  def currentUsage: WMemoryUsage = (
    getMemoryPoolMXBeans.asScala
    map (_.getPeakUsage)
    map (mu => WMemoryUsage(mu.getInit, mu.getUsed, mu.getCommitted, mu.getMax))
    reduceLeft (_ + _)
  )
  def showCurrentUsage() {
    println("\n    Elapsed: %.3f s".format((System.nanoTime - startTime) / 1000000000d))
    println(currentUsage)
  }
}

object TrackMemory {
  def premain(argString: String) {
    if (argString != null && argString != "")
      println("Memory Tracker arguments: " + argString)

    val pairs = (
      for {
        kv <- (argString split ';').toList
        idx = kv indexOf '='
        if idx > 0
      } yield (kv take idx, kv drop idx + 1)
    )
    new TrackMemory(pairs.toMap)
  }
}
