// in anticipation of being able to start the repl with
// a file contained in a jar.

import scala.tools.nsc._
import improving._

val g = improving.scalac.WGlobal()
import g._
