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
import util._

class WContexts {
  self: WGlobal =>
  
  import global._
  import analyzer.NoContext
  
  class WContext(val ctx: Context) {
    private val SanityCheckMax = 100
    private def mkList(f: Context => Context) =
      Iterator.iterate(ctx)(f) take SanityCheckMax takeWhile (_ != NoContext) toList
    
    def outers      = mkList(_.outer)
    def enclClasses = mkList(_.enclClass)
    def enclMethods = mkList(_.enclMethod)
    
    def imports      = ctx.imports map (x => WImportInfo(x))
    def importedSyms = imports flatMap (_.symbols)
    def implicitSyms = implicits map (_.sym)
    def implicits    = ctx.implicitss.flatten
    
    def conversions = {
      val pairs = implicitSyms filter (_.isMethod) collect {
        case x if x.paramss.nonEmpty && x.paramss.head.size == 1 =>
          x.paramss.head.head.tpe -> x.tpe.resultType
      }
      pairs groupBy (_._1) mapValues (_ map (_._2)) toMap
    }
    def showConversions = {
      val width = conversions.keys map (_.toString.length) max;
      val fmt = "%" + width + "s => %s"
      
      conversions.toList sortBy (_._1.toString) foreach {
        case (k, v) => println(fmt.format(k, v mkString " | "))
      }
    }
    
    def parameters = implicitSyms filter (_.isTerm) collect {
      case x if x.paramss.isEmpty || x.paramss.head.isEmpty || x.paramss.head.head.isImplicit => x
    }
  }
}

