import sbt._
import Keys._

object Improving extends Build {
  lazy val homeURL          = "https://www.github.com/paulp/scala-improving"
  lazy val improvingProject = Project("scala-improving", file(".")) settings (improvingSettings: _*)
  lazy val sonatype         = new SonatypeOSSPublisher(this, homeURL)

  lazy val projectSettings: Seq[Setting[_]] = Seq(
    resolvers += ScalaToolsSnapshots,
    name := "scala-improving",
    organization := "org.improving",
    version := "0.9.8-SNAPSHOT"
  )
  lazy val scalaSettings: Seq[Setting[_]] = Seq(
    resolvers += ScalaToolsSnapshots,
    scalaVersion := "2.10.0-SNAPSHOT",
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    scalaHome := Some(file("/scala/trunk/build/pack"))
  )
  lazy val dependencySettings: Seq[Setting[_]] = Seq(
    libraryDependencies <<= (scalaVersion)(
      sv => Seq(
        "org.scala-lang" % "scala-compiler" % sv,
        "javax.mail" % "mail" % "1.4.4",
        "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test",
        "org.scala-tools.testing" % "specs_2.9.1" % "1.6.9" % "test"
      )
    )
  )
  
  lazy val improvingSettings: Seq[Setting[_]] = (
    scalaSettings ++ projectSettings ++ dependencySettings ++ sonatype.sonatypeSettings ++ Seq(
      // parallelExecution := false,
      // fork := true
    )
  )
}
