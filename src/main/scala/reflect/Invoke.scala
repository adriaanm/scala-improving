/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package reflect

import scala.util.control.Exception.catching

/** A more convenient syntax for reflective invocation.
 *  
 *  Example usage:
 *  class Obj { private def foo(x: Int, y: String): Long = x + y.length }
 *
 *  You can call it reflectively one of two ways:
 *
 *  import improving.reflect._
 *  (new Obj) o 'foo(5, "abc")                  // the 'o' method returns Any
 *  val x: Long = (new Obj) oo 'foo(5, "abc")   // the 'oo' method casts to expected type.
 *
 *  If you call the oo method without giving the type inferencer
 *  some help, it will most likely infer Nothing and a ClassCastException
 *  will be the result.
 */

class SymbolWithArguments(val sym: ScalaSymbol, val args: AnyExt[_]*) {
  def getArgs     = args map (_.toAnyRef)
  def getArgTypes = args.toList map (_.toClass)
  def argsMatch(m: JMethod) =
    (m.getParameterTypes.toList, getArgTypes).zipped map (_ isAssignableFrom _) forall (_ == true)
      
  // only called if getMethod() fails - searches private methods too.
  def getDeclaredMethodsOn(x: AnyRef) =
    (x.getClass.getDeclaredMethods filter (_.getName == sym.name) find argsMatch) match {
      case Some(m)  => m setAccessible true ; m
      case None     => throw new NoSuchMethodException(sym.name)
    }
    
  def getMethodOn(x: AnyRef) =
    catching(classOf[NoSuchMethodException]) .
      opt (x.getClass.getMethod(sym.name, getArgTypes: _*)) .
      getOrElse (getDeclaredMethodsOn(x))

}
class RichSymbol(sym: ScalaSymbol) {
  def apply(args: AnyExt[_]*): SymbolWithArguments =
    new SymbolWithArguments(sym, args: _*)
}
class ReflectionOperators[T <: AnyRef](self: T) {
  val clazz = self.toClass
  
  /** Issue call without touching result - returns Any.
   */
  def o(sym: ScalaSymbol): Any = oo(new SymbolWithArguments(sym))
  def o(symApp: SymbolWithArguments): Any = oo(symApp)
  
  /** Issue call expecting return type R - casts result to R.
   */
  def oo[R](sym: ScalaSymbol): R = oo[R](new SymbolWithArguments(sym))
  def oo[R](symApp: SymbolWithArguments): R = {
    def method = symApp getMethodOn self
    method.invoke(self, symApp.getArgs: _*).asInstanceOf[R]
  }
}
