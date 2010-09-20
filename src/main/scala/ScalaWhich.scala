/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package util

/** scalawhich
 *  @author Paul Phillips <paulp@improving.org>
 *
 *  Prints the location of the class or source file for each class name given.
 */

import scala.tools.nsc._
import reporters.ConsoleReporter

class ScalaWhich(cp: String) {
  def this() = this("")
  
  val settings = new Settings(_ => ())
  settings.usejavacp.value = true
  if (cp != "")
    settings.classpath.value = cp
  
  val reporter  = new ConsoleReporter(settings)
  val global    = new Global(settings, reporter)
  val classPath = global.classPath
  
  private def cleanup(x: Any) = x.toString takeWhile (_ != '(')
  def find(name: String) = classPath.findClass(name) match {
    case Some(rep) if rep.binary.isDefined  => Some(cleanup(rep.binary.get))
    case Some(rep) if rep.source.isDefined  => Some(cleanup(rep.source.get))
    case _                                  => None
  }
  
  def show(args: String*) {
    args foreach { arg =>
      val path = find(arg) getOrElse "not found"
      println(arg + ": " + path)
    }
  }  
}

object ScalaWhich {
  def apply(cp: String): ScalaWhich = new ScalaWhich(cp)
    
  def main(args: Array[String]): Unit = {
    apply("").show(args: _*)
  }
}
