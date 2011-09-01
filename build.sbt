// ---------------------------------------------------------------------------
// Basic settings

resolvers ++= Seq(
  "Sonatype" at "https://oss.sonatype.org/content/groups/public",
  "Java.net Maven" at "http://download.java.net/maven/2/"
)

name := "scala-improving"

version := "0.9.7-SNAPSHOT"

organization := "org.improving"

scalaVersion := "2.9.1"
// scalaVersion := "2.10.0-SNAPSHOT"

// ---------------------------------------------------------------------------
// Additional compiler options and plugins

scalacOptions ++= Seq("-deprecation", "-unchecked")

// ---------------------------------------------------------------------------
// Other dependendencies

libraryDependencies <<= (scalaVersion)(sv => Seq("org.scala-lang" % "scala-compiler" % sv))

libraryDependencies ++= Seq(
  // sending email
  "javax.mail" % "mail" % "1.4.4",  // sending email
  "org.scala-tools.testing" % "scalacheck_2.9.0-1" % "1.9" % "test",
  "org.scala-tools.testing" % "specs_2.9.0-1" % "1.6.8" % "test"
)

// ---------------------------------------------------------------------------
// Publishing criteria
//
// this hackery causes publish-local to install to ~/.m2/repository instead of ~/.ivy
// 
// otherResolvers := Seq(Resolver.file("dotM2", file(Path.userHome + "/.m2/repository")))
// publishLocalConfiguration <<= (packagedArtifacts, deliverLocal, ivyLoggingLevel) map {
//   (arts, _, level) => new PublishConfiguration(None, "dotM2", arts, level)
// }

publishTo <<= (version) { (v: String) =>
  val oss = "https://oss.sonatype.org/content/repositories/"
  val staging = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  //
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at oss + "snapshots/") 
  else                             Some("releases"  at oss + "releases/")
}

publishMavenStyle := true

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
