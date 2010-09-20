/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

trait Reflection {
  def tryCast[T](x: Any)(implicit mf: Manifest[T]): Option[T] = 
    if (mf.erasure.isInstance(x)) Some(x.asInstanceOf[T]) else None
}

object Reflection extends Reflection
