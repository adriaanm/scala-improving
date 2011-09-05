import sbt._
import Keys._

/** Publishing to Sonatype from SBT is a *PITA*.
 */
class SonatypeOSSPublisher(build: Build, homeURL: String) {
  import build._

  val ossSnapshots = "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  val ossStaging   = "Sonatype OSS Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  val publishDry = TaskKey[Unit]("publish-dry")

  private def makeReplacer(pairs: (String, Any)*): String => String = {
    pairs.foldLeft(_: String) {
      case (res, (key, value)) => res.replaceAll("@@" + key + "@@", "" + value)
    }
  }
  private def templateArg(name: String) = "-D%s=@@%s@@".format(name, name)
  private def templateArgs = List("url", "repositoryId", "pomFile", "file", "packaging", "classifier") map templateArg
  private val mvnTemplate = "mvn -Dgpg.passphrase= gpg:sign-and-deploy-file " + templateArgs.mkString(" ")

  class Info(version: String, pomFile: File) {
    val pomDir       = pomFile.getParentFile
    val isSnapshot   = version.trim endsWith "-SNAPSHOT"
    val url          = ( if (isSnapshot) ossSnapshots else ossStaging ).root
    val repositoryId = if (isSnapshot) "sonatype-nexus-snapshots" else "sonatype-nexus-staging"

    def deployOne(artifact: Artifact, file: File, isDryRun: Boolean) = {
      val cmd0 = makeReplacer(
        "url"          -> url,
        "repositoryId" -> repositoryId,
        "pomFile"      -> pomFile.name,
        "file"         -> file.name,
        "packaging"    -> artifact.extension,
        "classifier"   -> artifact.classifier.getOrElse("")
      )(mvnTemplate)
      val cmd = cmd0.replaceAll("""-Dclassifier=($|\s+)""", "").trim

      if (isDryRun) println("Will run '" + cmd + "' in " + pomDir + ".")
      else Process(cmd, cwd = pomDir).!
    }
  }

  def generatePomExtra(scalaVersion: String) = (
    <parent>
      <groupId>org.sonatype.oss</groupId>
      <artifactId>oss-parent</artifactId>
      <version>7</version>
    </parent>
    <url>{ homeURL }</url>
    <build>
      <plugins>
        <plugin>
          <groupId>org.scala-tools</groupId>
          <artifactId>maven-scala-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>compile</goal>
                <goal>testCompile</goal>
              </goals>
            </execution>
          </executions>
          <version>{ scalaVersion }</version>
        </plugin>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-gpg-plugin</artifactId>
          <version>1.4</version>
          <executions>
            <execution>
              <id>sign-artifacts</id>
              <phase>verify</phase>
              <goals>
                <goal>sign</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
    <licenses>
      <license>
        <name>Simplified BSD License</name>
        <url>http://www.opensource.org/licenses/bsd-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>extempore</id>
        <name>Paul Phillips</name>
        <email>paulp@improving.org</email>
      </developer>
    </developers>
    <scm>
      <connection>scm:{ homeURL }</connection>
      <developerConnection>scm:{ homeURL }</developerConnection>
      <url>{ homeURL }</url>
    </scm>
  )
  
  def makePublishTask(isDry: Boolean) = {
    (version, makePom, packagedArtifacts) map { (v, p, as) =>
      val info = new Info(v, p)
      as foreach { case (a, f) => info.deployOne(a, f, isDry) }
    }
  }

  def sonatypeSettings: Seq[Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomIncludeRepository := ((x: MavenRepository) => false),
    pomExtra <<= (scalaVersion)(generatePomExtra(_)),
    publish <<= makePublishTask(false),
    publishDry <<= makePublishTask(true)
  )
}
