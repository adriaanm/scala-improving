/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package reflect

import scala.reflect.{ AnyValManifest, Manifest }

class ManifestExt[T](m: Manifest[T]) {
  import Manifest._

  private def isChar = m == Char
  private def charConformsTo = Set[ClassManifest[_]](Int, Long, Float, Double)
  private def weakIndex = WeakConformance.weakIndex(m)
  
  protected def weak_only_<:<(that: ClassManifest[_]) =
    if (m == Char) charConformsTo(that)
    else that match {
      case Boolean | Unit       => false
      case x: AnyValManifest[_] => weakIndex < WeakConformance.weakIndex(x)
      case _                    => false    
    }

  def weak_<:<(that: ClassManifest[_]) = (m <:< that) || (this weak_only_<:< that)
}
