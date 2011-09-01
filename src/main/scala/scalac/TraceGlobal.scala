package improving

import scala.tools.nsc._
import reporters._
import typechecker._
// import scala.tools.nsc.util.PostMortemTracer
// 
// class TraceGlobal(currentSettings: Settings, reporter: Reporter) extends Global(currentSettings, reporter) {
//   val showCall = new PostMortemTracer
//   
//   override lazy val analyzer = new {
//     val global: TraceGlobal.this.type = TraceGlobal.this
//   } with Analyzer {
//     override def newTyper(context: Context): Typer = new TraceTyper(context)
//     
//     class TraceTyper(context : Context) extends Typer(context) {
//       override def typed(tree: Tree, mode: Int, pt: Type): Tree = (
//         showCall[Tree]({ case t => t.tpe })("typed", tree.summaryString, modeString(mode), pt)(
//           super.typed(tree, mode, pt)
//         )
//       )
//     }
//   }
// }
