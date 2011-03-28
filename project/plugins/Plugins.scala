import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  lazy val findbugs4sbt = "de.johoop" % "findbugs4sbt" % "1.0.0"
}
