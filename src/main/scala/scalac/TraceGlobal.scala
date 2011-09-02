package improving

import scala.tools.nsc._
import reporters._
import typechecker._

trait TraceGlobalTrait {
  trait TraceAnalyzer extends Analyzer {
    import global._
    val show = new PostMortemTracer
    override def newTyper(context: Context): Typer = new TraceTyper(context)

    class TraceTyper(context : Context) extends Typer(context) {
      override def typed(tree: Tree, mode: Int, pt: Type): Tree = (
        show[Tree]({ case t => t.tpe })("typed", tree.summaryString, modeString(mode), pt)(
          super.typed(tree, mode, pt)
        )
      )
    }
  }  
}

class TraceGlobal(s: Settings, r: Reporter) extends Global(s, r) with TraceGlobalTrait { outer =>
  val global   = this
  
  override lazy val analyzer = new { val global: TraceGlobal.this.type = TraceGlobal.this } with TraceAnalyzer
}
