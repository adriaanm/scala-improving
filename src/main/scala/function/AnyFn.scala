/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package function

/** A helper trait for creating functions for which
 *  certain incoming types need custom implementations.
 */

trait AnyFn[+T] extends (Any => T) {
  def byte(x: Byte): T
  def short(x: Short): T
  def int(x: Int): T
  def long(x: Long): T
  def float(x: Float): T
  def double(x: Double): T
  def boolean(x: Boolean): T
  def char(x: Char): T
  def unit(x: Unit): T
  
  def ifAnyVal(x: AnyVal): T = sys.error("impossible")  // for completeness
  def ifAnyRef(x: AnyRef): T
  def ifNull(): T
  
  def apply(x: Any) = x match {
    case null         => ifNull()
    case x: Byte      => byte(x)
    case x: Short     => short(x)
    case x: Int       => int(x)
    case x: Long      => long(x)
    case x: Float     => float(x)
    case x: Double    => double(x)
    case x: Boolean   => boolean(x)
    case x: Char      => char(x)
    case x: Unit      => unit(x)
    case x: AnyRef    => ifAnyRef(x)
  }    
}

trait NumericFn[+T] extends AnyFn[T] {
  def ifIntegral(x: Long): T
  def ifFractional(x: Double): T
  
  def byte(x: Byte)     = ifIntegral(x)
  def short(x: Short)   = ifIntegral(x)
  def int(x: Int)       = ifIntegral(x)
  def long(x: Long)     = ifIntegral(x)
  def float(x: Float)   = ifFractional(x)
  def double(x: Double) = ifFractional(x)
}
trait NumericCharFn[+T] extends NumericFn[T] {
  def char(x: Char) = ifIntegral(x)
}
trait EmptyFn[+T] extends NumericCharFn[T] {
  def errorFn(x: Any): T

  def ifIntegral(x: Long)     = errorFn(x)
  def ifFractional(x: Double) = errorFn(x)
  def boolean(x: Boolean)     = errorFn(x)
  def unit(x: Unit)           = errorFn(x)
  def ifAnyRef(x: AnyRef)     = errorFn(x)
  def ifNull()                = errorFn(null)
}
class EmptyFnClass[+T](f: Any => T) extends EmptyFn[T] {
  def errorFn(x: Any): T = f(x)
}

object NumericFn {
  def apply[T](integral: Long => T, fractional: Double => T, error: Any => T): NumericFn[T] = {
    new EmptyFnClass(error) {
      override def ifIntegral(x: Long)     = integral(x)
      override def ifFractional(x: Double) = fractional(x)
    }
  }
}
