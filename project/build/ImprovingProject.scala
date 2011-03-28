/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2010 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

import sbt._
import de.johoop.findbugs4sbt._
import FindBugsReportType._

class ImprovingProject(info: ProjectInfo)
        extends DefaultProject(info)
           with FindBugs
           with ImprovingPublish {

  // repos
  val localMaven = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy   = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  val sonatype   = "Sonatype" at "https://oss.sonatype.org/content/groups/public"
  // mail.jar
  val javanetMaven = "Java.net Maven" at "http://download.java.net/maven/2/"

  // local use
  override def localScala = System.getenv("scala.local") match {
    case null   => super.localScala
    case path   => 
      log.info("Found scala.local: " + path)
      List(defineScala("2.9.0-local", new java.io.File(path)))
  }
  override lazy val findbugsReportType = FancyHtml
  override lazy val findbugsReportName = "findbugsReport.html"
  println("compileClasspath = " + compileClasspath)
  // override lazy val findbugsAnalyzedPath = compileClasspath.absString
    
  // override def consoleOptions = List(
  //   CompileOption("-Xnojline")
  // )

  // no idea how one is supposed to do this
  def replClasspathString: String = mainDependencies.scalaJars +++ compileClasspath absString;
  System.setProperty("improving.repl.classpath", replClasspathString)

  // sending email
  val javamail   = "javax.mail" % "mail" % "1.4.4" withSources() // "latest.integration"
  
  // testing
  val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test" withSources()
  val specs      = "org.scala-tools.testing" %% "specs" % "1.6.8" % "test" withSources()
}

// a dummy trait on the main branch, see the publish branch for implementation.
trait ImprovingPublish
