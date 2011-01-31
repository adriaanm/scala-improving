/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package comm

import java.net._

trait Browser {
  final val errMsg = "Error attempting to launch web browser"
  
  // XXX use java 6 Desktop methods if java is 6
  private def getOS = System.getProperty("os.name") match {
    case null                         => "unknown"
    case x if x startsWith "Mac"      => "mac"
    case x if x startsWith "Darwin"   => "mac"
    case x if x startsWith "Windows"  => "win"
    case _                            => "unix"
  }
  
  def openURL(url: String): Unit = try {
      getOS match {
        case "mac"    => launchMac(url)
        case "win"    => launchWin(url)
        case "unix"   => launchUnix(url)
        case _        => // do nothing
      }
    } catch { 
        case x        => throw x
    }
  
  private def launchMac(url: String) = {
    val fileMgr = Class.forName("com.apple.eio.FileManager");
    val openURL = fileMgr.getDeclaredMethod("openURL", classOf[String])
    openURL.invoke(null, url)
  }  
  private def launchWin(url: String) = Runtime.getRuntime.exec("rundll32 url.dll,FileProtocolHandler " + url)
  private def launchUnix(url: String): Unit = {
    val browsers = List("firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape")
    val browser = browsers.find(b => Runtime.getRuntime.exec(Array[String]("which", b)).waitFor == 0) getOrElse (return ())
    Runtime.getRuntime.exec(Array[String](browser, url))
  }
}

object Browser extends Browser