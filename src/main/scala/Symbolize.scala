/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving

import scala.reflect.NameTransformer.decode
import Symbolize._

trait Symbolize {
  def clazz: Class[_]
  def newUpdater(): Updater

  // implicit def createUpdater(x: Symbol.type): Updater = newUpdater()
  implicit def createUpdater(x: AnyRef): Updater = newUpdater()
}

object Symbolize {
  abstract class Updater {
    def update[T: Manifest](lhs: String, rhs: T): Unit
  }
}

trait ReflectSymbolize extends Symbolize {
  def clazz: Class[_] = ReflectSymbolize.this.getClass

  class ReflectiveUpdater extends Updater {
    val methods = clazz.getMethods.toList
    val setters = methods filter (_.getName endsWith "_$eq")
    def findSetter(name: String) = setters find (x => decode(x.getName).stripSuffix("_=") == name)
    val setterNames = setters map (x => decode(x.getName).stripSuffix("_="))

    def update[T: Manifest](lhs: String, rhs: T) {
      findSetter(lhs) match {
        case Some(setter) =>
          setter setAccessible true
          setter.invoke(ReflectSymbolize.this, rhs.asInstanceOf[AnyRef])
        case _            =>
          println("'" + lhs + "' doesn't exist in " + clazz)
      }
    }
  }
  
  def newUpdater() = new ReflectiveUpdater()
}

