/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

import sbt._

class ImprovingProject(info: ProjectInfo)
        extends DefaultProject(info)
           with ImprovingPublish {

  // repos
  val localMaven = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy   = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  val sonatype   = "Sonatype" at "https://oss.sonatype.org/content/groups/public"

  // local use
  override def localScala = System.getenv("scala.local") match {
    case null   => super.localScala
    case path   => 
      log.info("Found scala.local: " + path)
      List(defineScala("2.9.0-local", new java.io.File(path)))
  }
    
  // override def consoleOptions = List(
  //   CompileOption("-Xnojline")
  // )

  // no idea how one is supposed to do this
  def replClasspathString: String = mainDependencies.scalaJars +++ compileClasspath absString;
  System.setProperty("improving.repl.classpath", replClasspathString)
  
  // testing
  val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.7" % "test" withSources()
  val specs      = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test" withSources()
}

// a dummy trait on the main branch, see the publish branch for implementation.
trait ImprovingPublish
