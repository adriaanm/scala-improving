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

class TrackMemory {
  val startTime = System.nanoTime

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
  
  scala.sys addShutdownHook showCurrentUsage()
  Signallable("USR1", "Show memory utilization statistics.")(showCurrentUsage())
}

object TrackMemory {
  def premain(argString: String) { new TrackMemory }
}
