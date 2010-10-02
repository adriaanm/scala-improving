/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package util

// import reflect.Manifest

/**
 *  A convenience trait and class for simplifying the construction of equals and hashCode.
 *  If you use EqualsHelperClass, the arguments you pass to the constructor will be the
 *  values compared, and the type argument the type compared.
 *
 *{{{
    // notice only x is passed to the superclass
    class Measurement[T: Manifest](x: Int, y: Int) extends improving.util.Eq[T](x) { }
    class Velocity
    class Distance
    new Measurement[Velocity](5, 0) == new Measurement[Velocity](5, 0)      // true
    new Measurement[Velocity](5, 0) == new Measurement[Velocity](5, 3453)   // true
    new Measurement[Velocity](5, 0) == new Measurement[Distance](5, 0)      // false
 *}}}
 * 
 *  @author Paul Phillips
 */

class Eq[T: Manifest](_values: Any*) extends EqTrait {
  type EqualityType       = T
  def typeBasis           = manifest[T]
  override val valueBasis = _values.toSeq
}
  
trait EqTrait extends scala.Equals {
  // the type upon which equality will be based
  type EqualityType
  
  // the manifests which must be (deeply) equal
  def typeBasis: Manifest[EqualityType]
  
  // the list of values which must be pairwise equal.
  def valueBasis: Seq[Any] = Nil
  
  // the method used to compare pairwise values: defaults to ==.
  def valueComparator(x1: Any, x2: Any) = x1 == x2
  
  // a method to warn when we are compared to an unexpected type
  def optionalWarning(other: Any): Unit = ()
  
  final override lazy val hashCode: Int = {    
    (typeBasis +: valueBasis map calculateHashCode).foldLeft(hashSeed)((x, y) => x * 41 + y)
  }    
  
  def canEqual(that: Any) = that.isInstanceOf[EqTrait]
  final def equals(x: EqTrait): Boolean = (
    (typeBasis == x.typeBasis) &&
    (valueBasis corresponds x.valueBasis)(valueComparator)
  )
    
  /** Note that manifest equality is based on the erasure of T.
   *  We also compare type arguments.
   */
   final override def equals(other: Any): Boolean = other match {
     case x: EqTrait  => (this eq x) || (this equals x) 
     case _           => optionalWarning(other) ; false
   }
   
   protected def hashSeed: Int = "Entropy".##
   protected def calculateHashCode(x: Any) = x match {
     case null => 0
     case x    => x.##
   }
}

object EqTrait {
  // Normally we use "this" as a means for obtaining a manifest, but here we
  // have no value and utilize the type parameter directly.
  def apply[T: Manifest] = new EqTrait {
    type EqualityType = T
    override val typeBasis = manifest[T]
  }
}
