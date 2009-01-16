package org.improving.yourkit

object Profiler {  
  import com.yourkit.api._
  private var active = false
  lazy val controller = new Controller
    
  def start() = {    
    val thread = new Thread(
      new Runnable() {
        def run() = {
          active = true
          controller.startCPUProfiling(ProfilingModes.CPU_SAMPLING, Controller.DEFAULT_FILTERS)
        }
      }
    )
  
    thread setDaemon true  // let the application normally terminate
    thread.start
  }

  def save() = {
    controller.stopCPUProfiling()
    active = false
    controller.captureSnapshot(ProfilingModes.SNAPSHOT_WITHOUT_HEAP)
  }
  
  def isActive = active
}
