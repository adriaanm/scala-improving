/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package scalac

import scala.tools.nsc._

trait WInfos {
  self: WGlobal =>
  
  import global._
  import analyzer.{ ImplicitInfo }

  case class WImportInfo(val info: ImportInfo) {
    lazy val Import(qual, selectors) = info.tree
    
    def symbols       = info.allImportedSymbols
    def explicitNames = selectors map (_.rename) filterNot (_ == nme.WILDCARD) distinct
    def allNames      = symbols map (_.name) distinct
  }
  
  class WImplicitInfo(val info: ImplicitInfo) {
    val name = info.name
    val pre  = info.pre
    val sym  = info.sym
  
    def isMethod = sym.isMethod
    def isConversion = sym.paramss match {
      case (_ :: Nil) :: _  => true
      case _                => false
    }
    def fromSym = if (isConversion) sym.paramss.head.head else NoSymbol
    def toSym   = if (isConversion) sym.tpe.resultType.typeSymbol else NoSymbol
    def from    = fromSym.tpe
    def to      = toSym.tpe
  
    def matches(from1: Type, to1: Type) =
      (from1 <:< from) && (to <:< to1)
  }
}
