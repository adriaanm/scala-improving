import sbt._

class ImprovingProject(info: ProjectInfo)
        extends DefaultProject(info)
           with ImprovingPublish {

  // repos
  val localMaven    = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy      = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
}

// a dummy trait on the main branch, see the publish branch for implementation.
trait ImprovingPublish
