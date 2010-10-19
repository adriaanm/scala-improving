/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

import scala.reflect.OptManifest
import reflect._

trait Implicits {
  implicit def makeAnyRefExt[T <: AnyRef : OptManifest](x: T): AnyRefExt[T] = new AnyRefExt(x)
  implicit def makeAnyValExt[T <: AnyVal : OptManifest](x: T): AnyValExt[T] = new AnyValExt(x)
  implicit def makeAnyAnyExt(x: Any): AnyAnyExt = new AnyAnyExt(x)
  implicit def makeTraversableExt[A, CC[X] <: Traversable[X]](coll: CC[A]) = new TraversableExt[A, CC](coll)
  
  /** We also require an implicit on scala.Symbol so they appear to contain
   *  an apply method, which packages the method arguments.  The type parameter
   *  is the method's expected result type.
   */
  implicit def makeRichSymbol(sym: ScalaSymbol): RichSymbol = new RichSymbol(sym)
  
  /** An implicit on AnyRef provides it with the 'o' method, which is supposed
   *  to look like a giant '.' and present the feel of method invocation.
   */
  implicit def makeReflectionOperators[T <: AnyRef](x: T): ReflectionOperators[T] =
    new ReflectionOperators(x)
}
