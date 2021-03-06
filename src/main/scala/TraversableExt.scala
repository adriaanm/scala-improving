/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

import scala.collection.{ mutable, immutable, generic }
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom

/** Extra collections functions.
 */
class TraversableExt[A, CC[X] <: Traversable[X]](coll: CC[A]) {
  /** This will only be as good as the erased type.
   */
  def typeFilter[B <: AnyRef](implicit m: Manifest[B], bf: CanBuildFrom[CC[A], B, CC[B]]): CC[B] = {
    bf() ++= (coll collect reflect.typeCollector[B]) result
  }
  
  def distinctBy[T](f: A => T)(implicit eq: Equiv[T], bf: CanBuildFrom[CC[A], A, CC[A]]): CC[A] = {
    val buf = bf(coll)
    if (coll.isEmpty) buf.result
    else {
      val x = coll.head
      val xs = coll.tail
      buf += x
      buf ++= (xs filterNot (y => eq.equiv(f(x), f(y)))).distinctBy[T](f)
      buf.result
    }
  }

  def flatCollect[B](pf: PartialFunction[A, Traversable[B]])
    (implicit bf: CanBuildFrom[CC[A], B, CC[B]]): CC[B] =
  {
    val b = bf(coll)
    for (x <- coll collect pf)
      b ++= x

    b.result
  }
}
