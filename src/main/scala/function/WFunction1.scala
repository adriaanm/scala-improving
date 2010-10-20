/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package function

class NullSafeFn1[-T1, +R](f: T1 => R) extends (T1 => R) {
  def apply(x: T1): R = if (x == null) null.asInstanceOf[R] else f(x)
  override def compose[A](g: A => T1): A => R = { x => 
    val gn = new NullSafeFn1(g)
    apply(gn(x))
  }
  override def andThen[A](g: R => A): T1 => A = { x => 
    val gn = new NullSafeFn1(g)
    gn(apply(x))
  }
  
  def apply_?(x: T1) = apply(x)
  def compose_?[A](g: A => T1) = compose(g)
  def andThen_?[A](g: R => A) = andThen(g)
}

/** Given a starting value, the returned object can be repeatedly
 *  applied with Function1s and then the result retrieved with apply().
 *  At each iteration the argument is checked for null before function
 *  application; if it is ever null, the result will be null.
 * 
 *  <pre>
 *    case class Bop(next: Bop)
 *    val x = Bop(Bop(Bop(null)))
 *    ??(x)(_.next)()                         // returns Bop(Bop(null))
 *    ??(x)(_.next)(_.next)()                 // returns Bop(null)
 *    ??(x)(_.next)(_.next)(_.next)()         // returns null
 *    ??(x)(_.next)(_.next)(_.next)(_.next)() // still returns null!
 *  </pre>
 *
 *  @param  x The starting value
 *  @return   The ?? object, containing apply methods T => U and () => T
 */
// @experimental
// case class ??[T](x: T) {
//   def apply(): T = x
//   def apply[U >: Null](f: T => U): ??[U] =
//     if (x == null) ??[U](null)
//     else ??[U](f(x))
// }
