/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package reflect

object Numerical {
  import scala.math.ScalaNumber
  import java.lang.Character
  
  /** Analyzes the manifest to see if there's any chance this is a
   *  primitive or boxed primitive numeric.
   */
  def isPossiblyNumeric[T: ClassManifest](x: T): Boolean = isPossiblyNumeric[T]
  def isPossiblyNumeric[T: ClassManifest] : Boolean = {
    val m = classManifest[T]
    
    (m == Manifest.Any) ||
    (m == Manifest.Object) ||
    (m == classManifest[Character]) ||
    ((m <:< Manifest.AnyVal) && (m != Manifest.Boolean) && (m != Manifest.Unit)) ||
    ((m <:< classManifest[Number]) && !(m <:< classManifest[ScalaNumber])) ||
    (m == classManifest[Serializable]) ||
    (m == classManifest[Comparable[_]])
  }
}

/**   In order to encapsulate anything to do with reflection, we must
 *    overcome an issue with the boxing of primitives.  If we declare a
 *    method which takes arguments of type Any, by the time the
 *    method parameters can be examined, the primitives have already been boxed.
 *    The reflective call will then fail because classOf[java.lang.Integer]
 *    is not the same thing as classOf[scala.Int].
 *
 *    Any useful workaround will require examining the arguments before
 *    the method is called.  The approach here is to define different implicits
 *    for AnyVal and AnyRef which preserves their original identity as it
 *    transforms them.  We are left only with "Any" as a problem.
 */
sealed abstract class AnyExt[T](value: T) {
  def toManifest: Manifest[_ <: T]
  def toClass: JClass[_ <: T]
  def methods: List[JMethod]
  def fields: List[JField]

  /** Forward pipe */
  def |>[U](f : T => U) = f(value)

  /** Execute some side effecting code, then result in this value. */
  def tap(body: => Unit): T = {
    body
    value
  }
  
  /** conformance tests */
  def scala_<:<[T: Manifest](other: T) = toManifest <:< manifest[T]
  def weak_<:<[T: Manifest](other: T)  = toManifest weak_<:< manifest[T]

  def hasModuleName = toClass.getName endsWith "$"
  def toShortClassName: String = toClass.getName split '.' last
  def toAnyRef  = castTo[AnyRef]
  def castTo[T] : T = value.asInstanceOf[T]
  def safeTo[T: Manifest]: Option[T] = {
    if (manifest[T].typeArguments.nonEmpty) None    // can't be safe
    else if (manifest[T].erasure isAssignableFrom toClass) Some(castTo[T])
    else None
  }
}

final class AnyAnyExt(value: Any) extends AnyExt[Any](value) {
  def fields: List[JField]    = toClass.getFields.toList
  def methods: List[JMethod]  = toClass.getMethods.toList
  def toManifest: Manifest[_] = Manifest.classType(toClass)
  def toClass: JClass[_]      = toAnyRef.getClass
}

final class AnyValExt[T <: AnyVal : OptManifest](value: T) extends AnyExt(value) {
  def fields: List[JField]   = Nil
  def methods: List[JMethod] = Nil
  def toManifest: Manifest[T] = ClassManifest.fromClass(toClass).asInstanceOf[Manifest[T]]
  def toClass: Class[T] = (value match {
    case _: Byte    => classOf[Byte]
    case _: Short   => classOf[Short]
    case _: Int     => classOf[Int]
    case _: Long    => classOf[Long]
    case _: Float   => classOf[Float]
    case _: Double  => classOf[Double]
    case _: Char    => classOf[Char]
    case _: Boolean => classOf[Boolean]
    case _: Unit    => classOf[Unit]
  }).asInstanceOf[Class[T]]
}

final class AnyRefExt[T <: AnyRef : OptManifest](value: T) extends AnyExt(value) {
  private def optManifest = implicitly[OptManifest[T]]
  
  def outerField: Option[JField]    = fields find (_.getName == "$outer")
  def outerInstance: Option[AnyRef] = outerField map (_ get value)
  def outerChain: List[AnyRef]      = value :: {
    outerInstance map (_.outerChain) getOrElse Nil
  }
  
  def toCompanionName = toClass.getName + (
    if (hasModuleName) "" else "$"
  )
  def toCompanion: AnyRef     = Class forName toCompanionName getField "MODULE$" get null
  def fields: List[JField]    = toClass.getFields.toList
  def methods: List[JMethod]  = toClass.getMethods.toList
  def toManifest: Manifest[T] = (optManifest match {
    case x: Manifest[t]   => x
    case _                => Manifest.classType(toClass)
  }).asInstanceOf[Manifest[T]]

  def toClass: Class[T] = (optManifest match {
    case x: Manifest[t]   => x.erasure
    case _                => if (value == null) null else value.getClass
  }).asInstanceOf[Class[T]]
  
  /** It's not easy... to be... me... */
  def superMans: List[Manifest[_]] = supers map (_.toManifest)
  def superNames = supers map (_.getName)
  def interfaces: List[JClass[_]] = supers filter (_.isInterface)

  def supers: List[JClass[_]] = {
    def loop(x: JClass[_]): List[JClass[_]] = x.getSuperclass match {
      case null   => List(x)
      case sc     => x :: (x.getInterfaces.toList flatMap loop) ++ loop(sc)
    }
    loop(toClass).distinct
  }
}
