import sbt._

class ImprovingProject(info: ProjectInfo) extends DefaultProject(info) {  
  // disable sbt's scala jar handling so we can get sources easily
  // override def filterScalaJars = false
  // val scalac_280   = "org.scala-lang" % "scala-compiler" % "2.8.0" withSources()
  // val scalalib_280 = "org.scala-lang" % "scala-library" % "2.8.0" withSources()

  // bunch of repositories
  val localMaven   = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy     = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
}