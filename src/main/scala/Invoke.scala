/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package reflect

import scala.util.control.Exception.catching
import java.lang.{ Class => JClass }
import java.lang.reflect.{ Method => JMethod }
import scala.{ Symbol => ScalaSymbol }

/** A more convenient syntax for reflective invocation.
 *  
 *  Example usage:
 *  class Obj { private def foo(x: Int, y: String): Long = x + y.length }
 *
 *  You can call it reflectively one of two ways:
 *
 *  import improving.reflect.Invoke._
 *  (new Obj) o 'foo(5, "abc")                  // the 'o' method returns Any
 *  val x: Long = (new Obj) oo 'foo(5, "abc")   // the 'oo' method casts to expected type.
 *
 *  If you call the oo method without giving the type inferencer
 *  some help, it will most likely infer Nothing and a ClassCastException
 *  will be the result.
 */
 
object Invoke {
  /**   In order to encapsulate anything to do with reflection, we must
   *    overcome an issue with the boxing of primitives.  If we declare a
   *    method which takes arguments of type Any, by the time the
   *    method parameters can be examined, the primitives have already been boxed.
   *    The reflective call will then fail because classOf[java.lang.Integer]
   *    is not the same thing as classOf[scala.Int].
   *
   *    Any useful workaround will require examining the arguments before
   *    the method is called.  The approach here is to define two implicits,
   *    one for AnyRefs and one for AnyVals, and box them in a container
   *    which preserves their original class identity.
   */
  trait PrimitivePreserver[T] {
    val value: T
    val clazz: JClass[_]
  }
  case class PreservedAnyVal[T <: AnyVal](value: T) extends PrimitivePreserver[T] {
    val clazz = getAnyValClass(value)
  }
  case class PreservedAnyRef[T <: AnyRef](value: T) extends PrimitivePreserver[T] {
    val clazz = value.getClass
  }
  implicit def makePreservedAnyRef[T <: AnyRef](x: T) = PreservedAnyRef(x)
  implicit def makePreservedAnyVal[T <: AnyVal](x: T) = PreservedAnyVal(x)
  
  /** We also require an implicit on scala.Symbol so they appear to contain
   *  an apply method, which packages the method arguments.  The type parameter
   *  is the method's expected result type.
   */
  class SymbolWithArguments(val sym: ScalaSymbol, val args: PrimitivePreserver[_]*) {
    def getArgs = args map (_.value.asInstanceOf[AnyRef])
    def getArgTypes = args.toList map (_.clazz)
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
    def apply(args: PrimitivePreserver[_]*): SymbolWithArguments =
      new SymbolWithArguments(sym, args: _*)
  }
  implicit def makeRichSymbol(sym: ScalaSymbol): RichSymbol = new RichSymbol(sym)
  
  /** An implicit on AnyRef provides it with the 'o' method, which is supposed
   *  to look like a giant '.' and present the feel of method invocation.
   */
  class ReflectionOperators[T <: AnyRef](self: T) {
    val clazz = self.getClass.asInstanceOf[JClass[T]] 
    
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
  implicit def makeReflectionOperators[T <: AnyRef](x: T): ReflectionOperators[T] =
    new ReflectionOperators(x)
  
  /** Obtain the class object for an <code>AnyVal</code>.
   */
  def getAnyValClass(x: AnyVal): JClass[_] = x match {
    case _: Byte    => classOf[Byte]
    case _: Short   => classOf[Short]
    case _: Int     => classOf[Int]
    case _: Long    => classOf[Long]
    case _: Float   => classOf[Float]
    case _: Double  => classOf[Double]
    case _: Char    => classOf[Char]
    case _: Boolean => classOf[Boolean]
    case _: Unit    => classOf[Unit]
  }  
}
