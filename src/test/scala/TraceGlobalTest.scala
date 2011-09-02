package improving
package test

import scala.tools.nsc._
import io.File.makeTemp
import io.Directory.TmpDir
import org.specs._

// No matter what I do,
// warning: Failed to instantiate improving.TraceGlobal: 
//   java.lang.NoSuchMethodException: 
//   improving.TraceGlobal.<init>(scala.tools.nsc.Settings, scala.tools.nsc.reporters.Reporter)
//
object TraceGlobalSpec extends Specification {
  def newGlobal() = {
    val settings = new Settings()
    settings.embeddedDefaults(Thread.currentThread.getContextClassLoader())
    settings.globalClass.value = "improving.TraceGlobal"
    settings.d.value = TmpDir.get.path
    
    println(settings)

    Global(settings, new reporters.ConsoleReporter(settings))
  }
  def newSource(code: String) = {
    val src = makeTemp(prefix = "improving", suffix = "scala")
    src writeAll code
    src
  }
  
  "Tracing global traces" in {
    val global = newGlobal()
    val src    = newSource("class A { def bippy[T](x: T) = List(x, x) }")
    
    new global.Run compile List(src.path)
  }
}
