/* Browser
 * @author Paul Phillips
 *
 * Adapted from http://www.centerkey.com/java/browser/ which states
 * "Public Domain Software -- Free to Use as You Like"
 */

package org.improving.misc

import java.net._
// import reflect.Reflection._

object Browser {
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
