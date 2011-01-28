/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package object improving extends Implicits {
  type JClass[T]   = java.lang.Class[T]
  type JMethod     = java.lang.reflect.Method
  type ScalaSymbol = scala.Symbol
  
  // compat
  object sys {
    def error(msg: String) = Predef.error(msg)
  }
}
