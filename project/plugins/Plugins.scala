import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val localMaven   = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy     = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  
  val pluginRepo = "Pyrostream Repository" at "http://siasia.insomnia247.nl/repo-snapshots/"
  val github     = "net.ps" % "github-plugin" % "1.0"
}
