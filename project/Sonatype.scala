import sbt._
import Keys._

/** Publishing to Sonatype from SBT is a *PITA*.
 */
trait SonatypeOSS extends Build {
  def homeURL: String
  val ossSnapshots = "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  val ossStaging   = "Sonatype OSS Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

  private def makeReplacer(pairs: (String, Any)*): String => String = {
    pairs.foldLeft(_: String) {
      case (res, (key, value)) => res.replaceAll("@@" + key + "@@", "" + value)
    }
  }
  private def templateArg(name: String) = "-D%s=@@%s@@".format(name, name)
  private def templateArgs = List("url", "repositoryId", "pomFile", "file", "packaging", "classifier") map templateArg
  private val mvnTemplate = "mvn -Dgpg.passphrase gpg:sign-and-deploy-file " + templateArgs.mkString(" ")

  class Info(version: String, pomFile: File) {
    val pomDir       = pomFile.getParentFile
    val isSnapshot   = version.trim endsWith "SNAPSHOT"
    val url          = ( if (isSnapshot) ossSnapshots else ossStaging ).root
    val repositoryId = if (isSnapshot) "sonatype-nexus-snapshots" else "sonatype-nexus-staging"

    def deployOne(artifact: Artifact, file: File) = {
      val cmd = makeReplacer(
        "url"          -> url,
        "repositoryId" -> repositoryId,
        "pomFile"      -> pomFile.name,
        "file"         -> file.name,
        "packaging"    -> artifact.`type`,
        "classifier"   -> artifact.classifier.getOrElse("")
      )(mvnTemplate)

      println("Run " + cmd + " in " + pomDir)
      Process(cmd, cwd = pomDir).!
    }
  }

  def generatePomExtra() = (
    <build>
      <plugins>
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

  def sonatypeSettings: Seq[Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomIncludeRepository := ((x: MavenRepository) => false),
    pomExtra := generatePomExtra,
    publish <<= (version, makePom, packagedArtifacts) flatMap { (ver, pom, arts) =>
      val info = new Info(ver, pom)
      task(arts foreach { case (artifact, file) => info.deployOne(artifact, file) })
    }
  )
}
