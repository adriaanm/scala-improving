/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 *
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package scalac

import scala.tools.nsc._
import interpreter._
import scala.collection.{ mutable, immutable, generic }
import mutable.ListBuffer
import util.{ SourceFile, BatchSourceFile, ScriptSourceFile }

class WGlobal(g: Global) extends {
  val global: Global = g
  type ImportInfo = global.analyzer.ImportInfo
  type Context    = global.analyzer.Context

} with WContexts with WInfos {

  import global.{ treeWrapper => _, _ }
  import definitions.{ RootClass }
  import analyzer.NoContext

  lazy val repl = new IMain(g.settings)
  lazy val power = Power(repl)

  private implicit def stringToPhase(name: String): Phase = currentRun phaseNamed name

  // import analyzer._
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

  implicit def treeCollection(tree: Tree): List[Tree] = {
    val lb = new mutable.ListBuffer[Tree]
    global.treeWrapper(tree) foreach (lb += _)
    lb.toList
  }

  private def safeMembers(sym: Symbol) =
    if (sym.rawInfo.isComplete) sym.info.members else Nil

  lazy val allSyms    = mutable.HashSet[Symbol](global.definitions.RootClass)
  lazy val incomplete = allSyms filterNot (_.rawInfo.isComplete)
  def allSymbols: Set[Symbol] = {
    slurpSymbols()
    allSyms.toSet
  }

  private lazy val runSlurpSymbols: Unit = slurpSymbols()
  def slurpSymbols() = {
    val openingContents = allSyms.toSet
    val openingCount = openingContents.size
    allSyms.clear()

    var lastCount: Int = -1
    def loop(in: immutable.Set[Symbol]) {
      val thisCount = in.size
      if (lastCount == thisCount)
        return

      lastCount = thisCount
      val unseen = in filterNot allSyms
      val nextIn = unseen flatMap safeMembers
      allSyms ++= (unseen flatMap (_.ownerChain))

      loop(nextIn)
    }
    loop(openingContents)
    println("Symbols: %d -> %d".format(openingCount, allSyms.size))
    allSyms -- openingContents
  }

  def ph[T](body: => T): T    = atPhase(currentRun.typerPhase)(body)
  def phs: List[Phase]        = phaseNames map (currentRun phaseNamed _)
  def allph[T](op: => T)      = phs map (ph => (ph.name, atPhase(ph)(op)))
  def allphstr[T](op: => T)   = allph[T](op) map { case (ph, op) => "%15s -> %s".format(ph, op.toString take 240) }
  def showph(op: => Any)      = allphstr(op.toString) foreach (Console println _)

  def mkContext(code: String)       = analyzer.rootContext(mkTypedUnit(code))
  def mkSourceFile(code: String)    = new BatchSourceFile("<console>", code)
  def mkScriptFile(code: String)    = new ScriptSourceFile(mkSourceFile(code), code.toArray, 0)
  def mkScriptFiles(codes: String*) = codes.toList map mkScriptFile
  def mkUnit(code: String)          = new CompilationUnit(mkSourceFile(code))
  // def mkTree (code: String)         = power.mkTrees(code).head
  // def mkTrees(code: String)         = power.mkTrees(code)
  def mkRun(stopAfter: Phase)       = new Run { override def stopPhase(name: String) = name == stopAfter.next.name }
  def mkTyperRun()                  = mkRun("typer")
  def mkPicklerRun()                = mkRun("pickler")
  def mkErasureRun()                = mkRun("erasure")
  def mkJvmRun()                    = mkRun("jvm")
  // def mkType(id: String): Type      = repl.stringToCompilerType(id).asInstanceOf[Type]

  def missingWrap[T](body: => Type): Type =
    try body
    catch { case _: MissingRequirementError => NoType }

  def mkClass(name: String)  = missingWrap(definitions.getClass(newTermName(name)).tpe)
  def mkModule(name: String) = missingWrap(definitions.getModule(newTermName(name)).tpe)

  def executeRun(code: String, stopAfter: Phase, srcFn: String => SourceFile) = {
    reporter.reset
    val run =
      if (stopAfter.name == "terminal") new Run
      else mkRun(stopAfter)
    run compileSources List(srcFn(code))
    run.units.toList
  }

  def mkTypedUnit(code: String)              = executeRun(code, "terminal", mkSourceFile).head
  def mkFull(code: String)                   = executeRun(code, "terminal", mkSourceFile).head.body
  def mkPart(code: String, stopAfter: Phase) = executeRun(code, stopAfter, mkScriptFile).head.body
  def mkTyped(code: String): Tree            = mkPart(code, "typer")
  def mkPickled(code: String): Tree          = mkPart(code, "pickler")
  def mkErasured(code: String): Tree         = mkPart(code, "erasure")
  def mkJvmed(code: String): Tree            = mkPart(code, "jvm")

  class MoreTreeOps(tree: Tree) {
    def classStr(t: Tree) = t.getClass.getName split '.' last
    def treeStr(t: Tree)  = classStr(t) + ": " + t.tpe

    def defs = tree collect { case x: DefDef => x }
    def vals = tree collect { case x: ValDef => x }

    def deftps = ph(tree filter (_.isInstanceOf[DefDef]) map (_.tpe))
    def valtps = ph(vals map (_.tpe))

    def showtps = tree map (x => (x, x.tpe)) foreach {
      case (tree, tp) =>
        if (tp == null || tp == NoType) ()
        else println(classStr(tree) + ": " + tp)
    }
    def syms    = tree map (_.symbol) filterNot (x => x == null || x == NoSymbol)
    def tps     = ph(tree map (_.tpe) filterNot (x => x == null || x == NoType) distinct)
    def strs    = tree map treeStr
    def show    = strs foreach println
  }
  class StringTreeOps(code: String) {
    val tree = mkFull(code)

    def ?<(names: scala.Symbol*): Unit = {
      val strs = names map (_.name) toSet;
      tree.syms collect { case x if strs(x.name.toString) => showph(x.defString) }
    }
  }

  implicit def moreTreeOps(tree: Tree): MoreTreeOps = new MoreTreeOps(tree)
  implicit def stringTreeOps(code: String): StringTreeOps = new StringTreeOps(code)
  implicit def stringWContext(code: String): WContext = new WContext(mkContext(code))
}

object WGlobal {
  def mkSettings() = {
    val s = new Settings
    // sbt-related desperation
    System.getProperty("improving.repl.classpath") match {
      case null   => s.usejavacp.value = true
      case cp     => s.classpath.value = cp
    }
    s
  }

  def mkInteractive() = {
    val s      = mkSettings()
    val r      = new reporters.ConsoleReporter(s)
    val global = new interactive.Global(s, r) {
      override def validatePositions(tree: Tree) = ()
    }

    new global.Run()

    global
  }
  def mkGlobal() = {
    val s      = mkSettings()
    val r      = new reporters.ConsoleReporter(s)
    val global = new Global(s, r)

    new global.Run()
    global
  }

  implicit def treeFixer[T <: Global#Tree](x: Global#Tree): T = x.asInstanceOf[T]

  def apply(global: Global) = new WGlobal(global)
  def apply(): WGlobal = apply(mkGlobal())
}
