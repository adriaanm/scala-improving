import sbt._

class ImprovingProject(info: ProjectInfo)
        extends DefaultProject(info)
           with net.ps.GithubPlugin {  

  // repos
  val localMaven    = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy      = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  val pluginRepo    = githubRepo("siasia/plugin")
  val uploaderRepo  = githubRepo("siasia/uploader")
  val improvingRepo = githubRepo("paulp/scala-improving")

  // dependencies
  val uploader = "net.ps" % "github-uploader_2.7.7" % "1.0"
  
  // register credentials
  net.ps.github.Credentials(Path.userHome / ".github" / "credentials")
  
  // local use
  // override def localScala =
  //   defineScala("2.8.0-local", new java.io.File("/scala/trunk/target/pack")) :: Nil
  
  override val publishTo = Some(improvingRepo)
  override val publishToGithub = true
}