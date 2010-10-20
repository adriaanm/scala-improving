/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package scalac

import scala.tools.nsc._
import scala.collection.{ mutable, immutable, generic }
import mutable.ListBuffer
// import util._

class WGlobal(g: Global) extends {
  val global: Global = g
  type ImportInfo = global.analyzer.ImportInfo
  type Context    = global.analyzer.Context  
  
} with WContexts with WInfos {
  
  import global.{ treeWrapper => _, _ }
  import definitions.{ RootClass }
  import analyzer.NoContext
  // import analyzer._
  // 
  // type Context = analyzer.Context  
  // type NoContext = analyzer.NoContext  

  implicit def treeCollection(tree: Tree): List[Tree] = {
    val lb = new mutable.ListBuffer[Tree]
    global.treeWrapper(tree) foreach (lb += _)
    lb.toList
  }

  private val allSyms = new mutable.HashSet[Symbol]
  private def safeMembers(sym: Symbol) =
    if (sym.rawInfo.isComplete) sym.info.members else Nil
    
  private def populateSyms(in: immutable.Set[Symbol]) {
    val unseen: Set[Symbol] = in filterNot allSyms  
    allSyms ++= unseen

    val nextIn: Set[Symbol] = (unseen flatMap safeMembers) -- unseen
    if (nextIn.nonEmpty)
      populateSyms(nextIn)
  }
  def allSymbols = {
    if (allSyms.isEmpty)
      populateSyms(Set(RootClass))
    
    allSyms.toSet
  }

  // def ph[T](body: => T): T = atPhase(currentRun.typerPhase)(body)
  // 
  // object repower {  
  //   def mkContext(code: String)       = analyzer.rootContext(mkUnit(code))
  //   def mkSourceFile(code: String)    = new BatchSourceFile("<console>", code)
  //   def mkScriptFile(code: String)    = new ScriptSourceFile(mkSourceFile(code), code.toArray, 0)
  //   def mkScriptFiles(codes: String*) = codes.toList map mkScriptFile
  //   def mkUnit(code: String)          = new CompilationUnit(mkSourceFile(code))
  //   def mkTree (code: String)         = mkTrees(code).head
  //   def mkTrees(code: String)         = power.mkTrees(code)
  //   def mkRun(stopAt: String)         = new Run { override def stopPhase(name: String) = name == stopAt }
  //   def mkTyperRun()                  = mkRun("superaccessors")
  //   def mkPicklerRun()                = mkRun("pickler")
  //   def mkErasureRun()                = mkRun("erasure")
  //   def mkJvmRun()                    = mkRun("jvm")
  //   def mkType(id: String): Type      = repl.stringToCompilerType(id).asInstanceOf[Type]
  // 
  //   def missingWrap[T](body: => Type): Type =
  //     try body
  //     catch { case _: MissingRequirementError => NoType }
  // 
  //   def mkClass(name: String)  = missingWrap(definitions.getClass(newTermName(name)).tpe)
  //   def mkModule(name: String) = missingWrap(definitions.getModule(newTermName(name)).tpe)
  // 
  //   def mkFull(run: Run, code: String) = {
  //     reporter.reset
  //     run compileSources List(mkSourceFile(code))
  //     run.units.next.body
  //   }
  //   def mkPart(run: Run, code: String) = {
  //     reporter.reset
  //     run compileSources List(mkScriptFile(code))
  //     run.units.next.body
  //   }
  //   def mkTyped(code: String): Tree    = mkPart(mkTyperRun(), code)
  //   def mkPickled(code: String): Tree  = mkPart(mkPicklerRun(), code)
  //   def mkErasured(code: String): Tree = mkPart(mkErasureRun(), code)
  //   def mkJvmed(code: String): Tree    = mkPart(mkJvmRun(), code)
  // 
  //   class MoreTreeOps(tree: Tree) {
  //     def classStr(t: Tree) = t.getClass.getName split '.' last
  //     def treeStr(t: Tree)  = classStr(t) + ": " + t.tpe
  // 
  //     def defs = tree collect { case x: DefDef => x }
  //     def vals = tree collect { case x: ValDef => x }
  // 
  //     def deftps = ph(tree filter (_.isInstanceOf[DefDef]) map (_.tpe))
  //     def valtps = ph(vals map (_.tpe))
  // 
  //     def showtps = tree map (x => (x, x.tpe)) filterNot (x => x == null || x == NoType) foreach { case (tree, tp) => println(classStr(tree) + ": " + tp) }
  //     def syms    = tree map (_.symbol) filterNot (x => x == null || x == NoSymbol)
  //     def tps     = ph(tree map (_.tpe) filterNot (x => x == null || x == NoType) distinct)
  //     def strs    = tree map treeStr
  //     def show    = strs foreach println  
  //   }
  //   class StringTreeOps(code: String) {
  //     val tree = mkFull(new Run, code)
  // 
  //     def ?<(names: scala.Symbol*): Unit = {
  //       val strs = names map (_.name) toSet;
  //       tree.syms collect { case x if strs(x.name.toString) => showph(x.defString) }
  //     }
  //   }
  // 
  //   implicit def moreTreeOps(tree: Tree): MoreTreeOps = new MoreTreeOps(tree)
  //   implicit def stringTreeOps(code: String): StringTreeOps = new StringTreeOps(code)
  // 
  //   def phs: List[Phase]        = phaseNames map (currentRun phaseNamed _)
  //   def allph[T](op: => T)      = phs map (ph => (ph.name, atPhase(ph)(op)))
  //   def allphstr[T](op: => T)   = allph[T](op) map { case (ph, op) => "%15s -> %s".format(ph, op.toString take 240) }
  //   def showph(op: => Any)      = allphstr(op.toString) foreach (Console println _)
  // }
}
// 
// 
// val wrapper: GlobalWrapper[repl.compiler.type] = new GlobalWrapper[repl.compiler.type](repl.compiler)
// import wrapper.{ global => _, _ }
// import global.{ treeWrapper => _, _ }
// import repower._
// 
// implicit def treeFixer(x: Global#Tree): repl.compiler.Tree = x.asInstanceOf[repl.compiler.Tree]
// 
// val myCode = """
//   class A {
//     val barbar = 1
//     def f(quux: Int) = quux*barbar
//   }
// """
// 
// def mkInteractive() = {
//   val s = new Settings
//   val r = new reporters.ConsoleReporter(s)
//   val compiler = new interactive.Global(s, r) {
//     override def validatePositions(tree: Tree) = ()
//   }
//   
//   compiler
// }


// val src = mkSourceFile(forWhileCode)
// val interactive = mkInteractive()
// 
// val t: Tree = interactive.typedTree(src, true)
// 
// 

// 
// val analyzer  = global.analyzer
// val erasure   = global.erasure
// val infer     = typer.infer
// val jvm       = global.genJVM
// val parser    = global.syntaxAnalyzer
// val pickler   = global.pickler
// val typer     = global.typer
// 
// val NoContext = analyzer.NoContext
// type Context = analyzer.Context
// type ImplicitInfo = analyzer.ImplicitInfo

