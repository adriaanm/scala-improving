/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package reflect

import scala.reflect.AnyValManifest

object WeakConformance {
  import scala.reflect.Manifest._
  def numericTypes: List[AnyValManifest[_]] = List(Byte, Char, Short, Int, Long, Float, Double)
  
  def weakIndex(m: ClassManifest[_]) = m match {
    case Byte   => 0
    case Short  => 1
    case Int    => 2
    case Long   => 3
    case Float  => 4
    case Double => 5
    case _      => -1
  }
  
  def foldManifests(xs: Traversable[Manifest[_]]): Manifest[_] =
    xs.reduceLeft { (m1, m2) =>
      if (m1 weak_<:< m2) m2
      else if (m2 weak_<:< m1) m1
      else error(m1 + ", " + m2)
    }

  def unify[A, B, C](a: A, b: B)(implicit ev: weak_unify[A, B, C]): (C, C) = (ev.aToC(a), ev.bToC(b))
  
  implicit def weakSameSame[T]: weak_unify[T, T, T] = new weak_unify[T, T, T] {
    def aToC(x: T): T = x
    def bToC(x: T): T = x
  }

  sealed abstract class weak_<:<[A <: AnyVal, B <: AnyVal] extends (A => B)
  sealed abstract class weak_>:>[A <: AnyVal, B <: AnyVal] extends (B => A)
  sealed abstract class weak_unify[A, B, C] {
    def aToC(a: A): C
    def bToC(b: B): C
  }
    
  implicit val weakByteShort: weak_unify[Byte, Short, Short] = new weak_unify[Byte, Short, Short] {
    def aToC(x: Byte): Short = x
    def bToC(x: Short): Short = x
  }
  implicit val weakByteInt: weak_unify[Byte, Int, Int] = new weak_unify[Byte, Int, Int] {
    def aToC(x: Byte): Int = x
    def bToC(x: Int): Int = x
  }
  implicit val weakByteLong: weak_unify[Byte, Long, Long] = new weak_unify[Byte, Long, Long] {
    def aToC(x: Byte): Long = x
    def bToC(x: Long): Long = x
  }
  implicit val weakByteFloat: weak_unify[Byte, Float, Float] = new weak_unify[Byte, Float, Float] {
    def aToC(x: Byte): Float = x
    def bToC(x: Float): Float = x
  }
  implicit val weakByteDouble: weak_unify[Byte, Double, Double] = new weak_unify[Byte, Double, Double] {
    def aToC(x: Byte): Double = x
    def bToC(x: Double): Double = x
  }
  implicit val weakCharInt: weak_unify[Char, Int, Int] = new weak_unify[Char, Int, Int] {
    def aToC(x: Char): Int = x
    def bToC(x: Int): Int = x
  }
  implicit val weakCharLong: weak_unify[Char, Long, Long] = new weak_unify[Char, Long, Long] {
    def aToC(x: Char): Long = x
    def bToC(x: Long): Long = x
  }
  implicit val weakCharFloat: weak_unify[Char, Float, Float] = new weak_unify[Char, Float, Float] {
    def aToC(x: Char): Float = x
    def bToC(x: Float): Float = x
  }
  implicit val weakCharDouble: weak_unify[Char, Double, Double] = new weak_unify[Char, Double, Double] {
    def aToC(x: Char): Double = x
    def bToC(x: Double): Double = x
  }
  implicit val weakShortInt: weak_unify[Short, Int, Int] = new weak_unify[Short, Int, Int] {
    def aToC(x: Short): Int = x
    def bToC(x: Int): Int = x
  }
  implicit val weakShortLong: weak_unify[Short, Long, Long] = new weak_unify[Short, Long, Long] {
    def aToC(x: Short): Long = x
    def bToC(x: Long): Long = x
  }
  implicit val weakShortFloat: weak_unify[Short, Float, Float] = new weak_unify[Short, Float, Float] {
    def aToC(x: Short): Float = x
    def bToC(x: Float): Float = x
  }
  implicit val weakShortDouble: weak_unify[Short, Double, Double] = new weak_unify[Short, Double, Double] {
    def aToC(x: Short): Double = x
    def bToC(x: Double): Double = x
  }
  implicit val weakIntLong: weak_unify[Int, Long, Long] = new weak_unify[Int, Long, Long] {
    def aToC(x: Int): Long = x
    def bToC(x: Long): Long = x
  }
  implicit val weakIntFloat: weak_unify[Int, Float, Float] = new weak_unify[Int, Float, Float] {
    def aToC(x: Int): Float = x
    def bToC(x: Float): Float = x
  }
  implicit val weakIntDouble: weak_unify[Int, Double, Double] = new weak_unify[Int, Double, Double] {
    def aToC(x: Int): Double = x
    def bToC(x: Double): Double = x
  }
  implicit val weakLongFloat: weak_unify[Long, Float, Float] = new weak_unify[Long, Float, Float] {
    def aToC(x: Long): Float = x
    def bToC(x: Float): Float = x
  }
  implicit val weakLongDouble: weak_unify[Long, Double, Double] = new weak_unify[Long, Double, Double] {
    def aToC(x: Long): Double = x
    def bToC(x: Double): Double = x
  }
  implicit val weakFloatDouble: weak_unify[Float, Double, Double] = new weak_unify[Float, Double, Double] {
    def aToC(x: Float): Double = x
    def bToC(x: Double): Double = x
  }
  
  def generateWeakUnify = {
    for (m1 <- numericTypes ; m2 <- numericTypes) {
      val conforms = m1 match {
        case Char => List(Int, Long, Double, Float) contains m2
        case _    => weakIndex(m1) < weakIndex(m2)
      }

      val template =
        """|implicit val weak%s%s: %s = new %s {
           |  def aToC(x: %s): %s = x
           |  def bToC(x: %s): %s = x
           |}"""
    
      val typeString = "weak_unify[%s, %s, %s]".format(m1, m2, m2)
    
      if (conforms) {
        println(template.stripMargin.format(
          m1, m2,
          typeString, typeString,
          m1, m2,
          m2, m2
        ))
      }
    }
  }
}
