/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

package object reflect {  
  /** This will only be as good as the erased type.
   */
  def typeCollector[A <: AnyRef: Manifest] : PartialFunction[Any, A] = {
    val erasure = manifest[A].erasure;
    
    { case x: AnyRef if erasure.isAssignableFrom(x.getClass)  => x.asInstanceOf[A] }
  }
  
  def manifests(xs: AnyExt[_]*): List[Manifest[_]] = xs.toList map (_.toManifest)
}