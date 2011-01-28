/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package reflect

import java.util.concurrent.{ Future, TimeUnit }
import java.lang.reflect.{ Modifier, Field, Method => JMethod }
import LazyInfo._

/** Utility class for extracting lazy val information.
 *
 *  Unlikely todo: read the Code segment, parse the lazy
 *  accessors, and see what constant is being used as a mask.
 *  That would insulate us from the precise algorithm used to demux
 *  the bitmaps.  We'd find a series of instructions like this:
 *
 *    16:	getfield	#22;  // Field bitmap$0:I
 *    19:	bipush	64      // <-- that's mr. mask
 *    21:	iand
 *    22:	iconst_0
 *    23:	if_icmpne	...
 *
 *  In a similar vein, rather than making people supply both the
 *  field name and the T => U, we could open up the T => U and
 *  dredge it to figure out the field.
 */
class LazyInfo[T <: AnyRef](underlying: T) {
  info =>

  private def clazz               = underlying.getClass
  private def clazzName           = clazz.getName split '.' last
  private def methods             = clazz.getMethods.toList
  private def fields              = clazz.getFields.toList
  private def decls               = clazz.getDeclaredFields.toList
  private def isPrivate(f: Field) = Modifier.isPrivate(f.getModifiers())
  private def isFinal(f: Field)   = Modifier.isFinal(f.getModifiers())
  private def hasSetter(f: Field) = {
    val setterName = f.getName + "_$eq"
    def isSetter(m: JMethod)      = m.getName == setterName
    def isTraitSetter(m: JMethod) = m.getName endsWith ("$_setter_$" + setterName)
    
    methods exists (m => isSetter(m) || isTraitSetter(m))    
  }
  private def onError(msg: String) = sys.error("No lazy val " + msg)
  private def bitmapNum(f: Field)  = f.getName dropWhile (_ != '$') drop 1 toInt
  def bitmapNamed(name: String)    = bitmaps find (_.getName == name)
  def accessorNamed(name: String)  = lazyFields find (_.getName == name)
  
  /** The bitmap fields in this class.
   */
  def bitmaps: List[Field]        = fields filter (_.getName startsWith "bitmap$")
  
  /** The java reflection Fields for each lazy val.  Exploits the
   *  facts that 1) vars have setters and 2) non-lazy vals are final.
   */
  def lazyFields = decls filter (f => isPrivate(f) && !isFinal(f) && !hasSetter(f))
  
  /** A map from names to nullary functions which evaluate to true if
   *  the lazy val of that name has been forced.
   */
  lazy val lazinessMap: Map[String, () => Boolean] = {
    lazyFields.zipWithIndex map {
      // XXX being sloppy wrt the Xcheckinit bits which are also crammed in here.
      case (f, i) => f.getName -> (() => List(i * 2, i * 2 + 1) exists hasEvaluatedOffset)
    } toMap
  }
  def lazinessList = lazinessMap.toList sortBy (_._1)
  
  /** It's a Future, but ignoring all the cancellation and timeout business.
   *  This one is all about isDone and get().
   */
  class ObservableLazyVal[U] private[LazyInfo](name: String, accessor: T => U) extends Future[U] {
    def isDone()                               = info(name)
    def get(): U                               = accessor(underlying)
    def get(timeout: Long, unit: TimeUnit): U  = get()
    def isCancelled()                          = false
    def cancel(mayInterruptIfRunning: Boolean) = false

    private def valueString                    = if (isDone) get().toString else "<lazy>"
    override def toString                      = "%s#%s: %s".format(clazzName, name, valueString)
  }
  def futurize[U](name: String, accessor: T => U) =
    new ObservableLazyVal[U](name, accessor)

  def apply(name: String): Boolean = get(name) getOrElse onError("called '" + name + "'")
  def apply(offset: Int): Boolean  = get(offset) getOrElse onError("at bitmap index " + offset)

  def get(name: String): Option[Boolean] = lazinessMap get name map (f => f())
  def get(offset: Int): Option[Boolean] = {
    val name    = BITMAP_PREFIX + (offset / FLAGS_PER_WORD)
    val mask    = 1 << (offset % FLAGS_PER_WORD)
    
    bitmapNamed(name) map (f => ((f getInt underlying) & mask) != 0)
  }
  def show() = {
    forcedNames foreach (x => println(x + ": forced"))
    lazyNames foreach (x => println(x + ": unevaluated"))
  }
  
  def hasEvaluated(name: String)        = get(name) exists (_ == true)
  def hasNotEvaluated(name: String)     = get(name) exists (_ == false)
  def hasEvaluatedOffset(index: Int)    = get(index) exists (_ == true)
  def hasNotEvaluatedOffset(index: Int) = get(index) exists (_ == false)
  
  def isAllForced = lazinessMap.values forall (f => f())
  def isAnyForced = lazinessMap.values exists (f => f())
  def isAllLazy   = lazinessMap.values forall (f => !f())
  def isAnyLazy   = lazinessMap.values exists (f => !f())
  def forcedNames = lazinessList collect { case (x, f) if f() => x }
  def lazyNames   = lazinessList collect { case (x, f) if !f() => x }
}

object LazyInfo {
  implicit def anyRefCanLazify[T <: AnyRef](x: T) = new {
    def lazify: LazyInfo[T] = apply(x)
  }

  val FLAGS_PER_WORD = 32
  def BITMAP_PREFIX = "bitmap$"
  
  def apply[T <: AnyRef](x: T) = new LazyInfo(x)
}
