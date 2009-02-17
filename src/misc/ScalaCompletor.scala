/* NSC -- new Scala compiler
 * Copyright 2005-2008 LAMP/EPFL
 * @author Paul Phillips
 */
// $Id$

package org.improving.misc

import scala.tools.nsc.{ util, Settings }
import jline._
import reflect.Reflection._
import java.io.File
import java.util.jar.{ JarEntry, JarFile }

object ScalaCompletor {
  final val filter = new SimpleCompletor.SimpleCompletorFilter {
    def filter(element: String): String = if (element contains "$") null else element
  }
  
  // non-private, declared in this class (not subclasses) fields and methods
  def memberNames(clazz: Class[_]) = {
    new SClass(clazz)
    . declarations
    . filter(!_.isSynthetic)
    . map(_.getIdentifier)
    . removeDuplicates
    . filter(x => !(x contains "$"))        // !synthetic catches most, but $tag and some scala shuffling remains
    . sort((x, y) => x < y)
  } 
  
  def classNames(file: File) = {
    val classes: List[String] = 
      if (file.isDirectory) getClassFilesFromDir(file)
      else getClassFilesFromJar(file)
    
    // now filter classes by changing "/" to "." and trimming the trailing ".class"
    classes.map(x => x.replace('/', '.').substring(0, x.length - 6))
  }
  
  def getClassFilesFromDir(dir: File) = {
    def allFiles(f: File): List[File] = 
      if (f == null) Nil
      else if (f.isDirectory) f.listFiles.toList flatMap allFiles
      else List(f)
    val root = dir.getAbsolutePath

    for {
      file <- allFiles(dir)
      val name = file.getAbsolutePath
      if name startsWith root
      if file.getName endsWith ".class"
    } yield {
      name.substring(root.length + 1)
    }
  }
  
  def getClassFilesFromJar(file: File) = {
    def enumToList[T](e: java.util.Enumeration[T], xs: List[T]): List[T] =
      if (e == null || !e.hasMoreElements) xs else enumToList(e, e.nextElement :: xs)
    
    enumToList(new JarFile(file).entries, Nil)
    . map(_.getName)
    . filter(_ endsWith ".class")
  }
  
  def classfiles(cp: util.ClassPath#Context): Array[File] = cp.entries.map(_.location.file).toArray
  def allClassNames(cp: util.ClassPath#Context): Array[String] = classfiles(cp) flatMap classNames
  
  def classpathFromSettings(settings: Settings) = {
    import util.ClassPath
    val cpobj = new ClassPath(false)
    val cp = new cpobj.Build(settings.classpath.value, settings.sourcepath.value,
                         settings.outdir.value, settings.bootclasspath.value,
                         settings.extdirs.value, settings.Xcodebase.value)
    
    cp.root
  } 
  def classloaderFromSettings(settings: Settings) = {
    val urls = classfiles(classpathFromSettings(settings)).map(_.toURL)
    new java.net.URLClassLoader(urls, null)
  }
}

import ScalaCompletor.{ allClassNames, filter }

class ScalaCompletor(cp: util.ClassPath#Context) extends SimpleCompletor(allClassNames(cp), filter) {
  import ScalaCompletor._
  import java.util.{ List => JList }
  
  setDelimiter(".")
  val classloader = new java.net.URLClassLoader(classfiles(cp).map(_.toURL), null)
        
  private def getClassByName(s: String): Option[Class[_]] = 
    org.improving.reflect.Reflection.getClassByName(s, classloader)

  def showDocumentation(s: String): Unit = {
    import Browser.openURL
    val scalaRoot = "http://www.scala-lang.org/docu/files/api/"
    val javaRoot = "http://java.sun.com/j2se/1.5.0/docs/api/"

    def mkRoot(s: String) = if (s startsWith "scala.") scalaRoot else if (s startsWith "java.") javaRoot else ""
    def mkPath(s: String) = s.replace('.', '/') + ".html"
    def mkURL(s: String) = mkRoot(s) + mkPath(s)
    
    // only docs for these right now
    if (!s.startsWith("java.") && !s.startsWith("scala.")) return
    
    // try it as is
    if (getClassByName(s).isDefined) return openURL(mkURL(s))
    
    // drop everything after the last dot and try again
    val path = s.substring(0, s.lastIndexOf('.'))
    if (getClassByName(path).isDefined) return openURL(mkURL(path))
    
    return 
  }

  override def complete(buffer: String, cursor: Int, candidates: JList[_]): Int = {
    if (buffer == null) return 0
    val clist = candidates.asInstanceOf[JList[String]]
    val lastDot = buffer.lastIndexOf('.')
    val (path, stub) = 
      if (lastDot < 0) (buffer, "")
      else (buffer.substring(0, lastDot), buffer.substring(lastDot + 1))
            
    def membersCompletion(clazz: Class[_], stub: String): Int = {      
      val mems = memberNames(clazz)
      
      // if the whole method name is present, we complete with prototypes
      if (mems contains stub)
        methodCompletionList(new SClass(clazz).signatures(stub))
      else {
        // insert everything that starts with stub
        mems.filter(_ startsWith stub).foreach(x => clist add x)
        buffer.length - stub.length
      }
    }
    
    def methodCompletionList(xs: List[String]) = {
      clist add " "  // dummy space so file completion doesn't trigger
      xs foreach { clist add _ }
      buffer.length
    }
    
    // first try it as-is - if it's a class, show constructors
    getClassByName(buffer) match {
      case Some(clazz)    => 
        val cons = new SClass(clazz) constructorSignatures
        
        if (!cons.isEmpty) return methodCompletionList(cons)
      case None           => 
    }
    
    // next split it at the dot and see if it's a class and a (possibly empty) stub
    getClassByName(path) match {
      case Some(clazz)    => return membersCompletion(clazz, stub)
      case None           => 
    }
    
    // otherwise look to superclass for regular class completion
    val where = super.complete(buffer, cursor, candidates)
    // overrule superclass on adding a space to unique matches
    if (clist.size == 1 && clist.get(0).endsWith(" ")) {
      def chop(s: String) = s.substring(0, s.length - 1)
      clist.set(0, chop(clist.get(0)))
    }
    where
  }
}
