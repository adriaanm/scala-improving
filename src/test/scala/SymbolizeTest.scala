package improving.test

import scala.tools.nsc._
import interpreter.IMain
import improving._
import org.specs._

object SymbolizeTest {
  class UsesRepl extends IMain with Symbolize {
    class ReplUpdater extends Symbolize.Updater {
      def update[T: Manifest](lhs: String, rhs: T): Unit = {
        bind(lhs, manifest[T].toString, rhs)
      }
    }

    def clazz = UsesRepl.this.getClass
    def newUpdater() = new ReplUpdater
  }
  class UsesReflection extends ReflectSymbolize {
    var quux: Int = 0
  }
}
import SymbolizeTest._

object SymbolizeSpec extends Specification {
  // still too hard to figure out repl/classpath business in sbt
  //
  // "symbolize via repl" in {
  //   val repl = new UsesRepl
  //   import repl._
  // 
  //   'x = 5
  //   'y = 10
  //   'z = 15
  //   
  //   repl.evalExpr[Int]("x * y * z") must be equalTo(5 * 10 * 15)
  // }
  
  "symbolize via reflection" in {
    val reflects = new UsesReflection
    import reflects._
    
    quux must be equalTo(0)
    'quux = 30
    quux must be equalTo(30)
  }
}
