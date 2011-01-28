/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

/** A syntax for literal lists for people who can't handle
 *  companion object applies.
 */
object lit {  
  class TerminateList[T](xs: T*) { def > : List[T] = xs.toList }
  class TerminateSet[T](xs: T*)  { def * : Set[T] = xs.toSet }
  class TerminateMap[T, U](xs: (T, U)*) { def % : Map[T, U] = xs.toMap }
  
  def <[T](xs: T*)         = new TerminateList(xs: _*)
  def *[T](xs: T*)         = new TerminateSet(xs: _*)
  def %[T, U](xs: (T, U)*) = new TerminateMap(xs: _*)

  def demoMap  = %( 1 -> 2, 5 -> 6, 10 -> 11 )%
  def demoList = <( 5, 10, 15, 20, 15, 10, 5 )>
  def demoSet  = *(5, 10, 15, 10, 5)*
}