import sbt._
import Keys._

object Improving extends Build with SonatypeOSS {
  lazy val homeURL          = "https://www.github.com/paulp/scala-improving"
  lazy val improvingProject = Project("scala-improving", file(".")) settings (improvingSettings: _*)

  lazy val projectSettings = Seq(
    resolvers += ScalaToolsSnapshots,
    name := "scala-improving",
    organization := "org.improving",
    version := "0.9.7"
  )
  lazy val localSettings = Seq(
    // parallelExecution := false,
    // fork := true
  )
  lazy val buildSettings = Seq(
    resolvers += ScalaToolsSnapshots,
    scalaVersion := "2.10.0-SNAPSHOT",
    scalaHome := Some(file("/scala/trunk/build/pack")),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    libraryDependencies <<= (scalaVersion)(
      sv => Seq(
        "org.scala-lang" % "scala-compiler" % sv,
        "javax.mail" % "mail" % "1.4.4",
        "org.scala-tools.testing" % "scalacheck_2.9.0-1" % "1.9" % "test",
        "org.scala-tools.testing" % "specs_2.9.0-1" % "1.6.8" % "test"
      )
    )
  )
  
  lazy val improvingSettings = projectSettings ++ buildSettings ++ sonatypeSettings
}
