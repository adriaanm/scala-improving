/*
 *
 * @author Paul Phillips <paulp@improving.org>
 * Inspired by the java version by Mark Petrovic available at
 *   http://www.onjava.com/pub/a/onjava/2007/01/03/discovering-java-security-requirements.html
 *
*/
package org.improving

import java.lang.reflect.Field
import java.net.URL
import java.security.{ AccessController, AccessControlContext, CodeSource, Permission, ProtectionDomain }
import scala.collection.immutable

class SM extends SecurityManager
{
  import SM._
  val thisCodeSource = this.getClass.getProtectionDomain.getCodeSource
  val thisCodeSourceURL = thisCodeSource.getLocation.toString
  var cache = Set[Rule]()
  
  // helper bits so we don't recurse forever on security checks we caused ourselves
  val recursionMarker = (this.getClass.getName, "buildRules")   // name/class marker tuple
  def isRecur(x: StackTraceElement) = recursionMarker == (x.getClassName, x.getMethodName)
  def isRecursion(t: Throwable) = t.getStackTrace exists isRecur
  
  // these two are the choke points for all permissions checks
  override def checkPermission(perm: Permission): Unit =
    checkPermission(perm, AccessController.getContext)  
  override def checkPermission(perm: Permission, context: Object): Unit =
    if (!isRecursion(new Throwable))
      buildRules(perm, context.asInstanceOf[AccessControlContext])
  
  // overridden for our convenience -- just widening it to public
  override def getClassContext() = super.getClassContext()
  
  private def buildRules(perm: Permission, context: AccessControlContext) =
    for (pd <- getProtectionDomains(context)) addToCache(perm, pd)
  
  private def getProtectionDomains(context: AccessControlContext): List[ProtectionDomain] = {
    def extractPD(f: Field) = {
      f setAccessible true
      (f get context).asInstanceOf[Array[ProtectionDomain]].toList
    }
    
    onull(classOf[AccessControlContext].getDeclaredFields) match {
      case None     => Nil
      case Some(xs) => (xs.find(_.getName == "context") map extractPD) getOrElse Nil
    }
  }
  
  def addToCache(perm: Permission, pd: ProtectionDomain) =
    Rule(perm, pd) match {
      case Some(rule) if !(cache contains rule) => cache += rule ; println(rule)
      case _                                    =>
    }
  override def toString() = "SecurityManager (" + cache.size + " rules in cache)"
}

object SM
{
  def onull[T](x: T): Option[T] = if (x == null) None else Some(x)
  
  // factory method validates input and invokes private constructor
  object Rule {
    def apply(perm: Permission, pd: ProtectionDomain): Option[Rule] = {
      val cs = onull(pd.getCodeSource) getOrElse (return None)
      val url = onull(cs.getLocation) getOrElse (return None)
      
      Some(new Rule(perm, pd, url.toString))
    }
  }
  class Rule private (
    val perm: Permission,
    val pd: ProtectionDomain,
    val url: String)
  {
    def esc(s: String) = s.replace("\"","\\\"").replace("\r","\\\r") 
    lazy val grantStr = """grant codeBase "%s" """.format(url)
    lazy val permStr  = """permission %s "%s", "%s";""".format(perm.getClass.getName, esc(perm.getName), perm.getActions)
    
    override def toString() = "%s { %s }".format(grantStr, permStr)
  }
  
  // return a policy file as a string
  def policy(): String = {
    val cache = System.getSecurityManager match {
      case sm: SM   => sm.cache // .toList
      case _        => return "SM not installed.\n"
    }
    
    // fold the cache into one map per codebase and format the string
    val strs = for ((cb, rules) <- (cache groupBy (_.url))) yield
      rules.toList.map(_.toString).sort(_ < _).mkString("grant codebase \"" + cb + "\" {\n  ", "\n  ", "\n}\n")
    
    strs.mkString("\n")
  }  
}