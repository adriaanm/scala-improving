import sbt._

class ImprovingProject(info: ProjectInfo)
        extends DefaultProject(info)
           with ImprovingPublish {

  // repos
  val localMaven    = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy      = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"

  // local use
  override def localScala = System.getenv("scala.local") match {
    case null   => Nil
    case path   => List(defineScala("2.8.0-local", new java.io.File(path)))
  }
}

// a dummy trait on the main branch, see the publish branch for implementation.
trait ImprovingPublish
