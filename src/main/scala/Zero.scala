/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

import scala.collection._

/** A trait representing zero.
 *
 * @author Paul Phillips (standing on the shoulders of giants)
 * @since 2.8
 */

trait Zero[+Z] {
  val zero: Z  
}

object Zero {
  def apply[Z](implicit z: Zero[Z])   = z.zero
  def zero[Z](z: Z)                   = new Zero[Z] { val zero = z }
  
  implicit object UnitZero extends Zero[Unit] { val zero = () }
  implicit object StringZero extends Zero[String] { val zero = "" }
  implicit object BooleanZero extends Zero[Boolean] { val zero = false }
  implicit object ByteZero extends Zero[Byte] { val zero = 0.toByte }
  implicit object ShortZero extends Zero[Short] { val zero = 0.toShort }
  implicit object IntZero extends Zero[Int] { val zero = 0 }
  implicit object LongZero extends Zero[Long] { val zero = 0l }
  implicit object CharZero extends Zero[Char] { val zero = 0.toChar }
  implicit object FloatZero extends Zero[Float] { val zero = 0f }
  implicit object DoubleZero extends Zero[Double] { val zero = 0d }
  implicit object BigIntZero extends Zero[BigInt] { val zero = BigInt(0) }
  implicit object BigDecimalZero extends Zero[BigDecimal] { val zero = BigDecimal(0) }
  
  implicit def OptionZero[A] = zero[Option[A]](None)
  
  implicit def TraversableZero[A] = zero[Traversable[A]](Traversable.empty)
  implicit def IterableZero[A] = zero[Iterable[A]](Iterable.empty)
  implicit def SeqZero[A] = zero[Seq[A]](Seq.empty)
  implicit def VectorZero[A] = zero[Vector[A]](Vector.empty)
  
  implicit def ArrayZero[A: ClassManifest] = zero(new Array[A](0))
  implicit def ListZero[A] = zero[List[A]](Nil)
  implicit def StreamZero[A] = zero[Stream[A]](Stream.empty)
  
  implicit def SetZero[A] = zero[Set[A]](Set.empty)
  implicit def MapZero[A,B] = zero[Map[A,B]](Map.empty)
}