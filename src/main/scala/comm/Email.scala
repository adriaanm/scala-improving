/** Improving: An unconstrained collection of scala code.
 *  Copyright 2005-2011 Paul Phillips
 * 
 *  Distributed under the "Simplified BSD License" in LICENSE.txt.
 */

package improving
package comm

import javax.mail._
import javax.mail.internet._
import java.util.Properties

trait Email {
  def newSession() = {
    val props = new Properties()
    props.put("mail.smtps.auth", "true")
  
    val session = Session.getInstance(props, null)
    session setDebug true
    session
  }

  def newMessage(session: Session)(f: MimeMessage => Unit) = {
    val msg = new MimeMessage(session)
    f(msg)
    msg
  }

  case class EmailServer(host: String, username: String, password: String) {
    val addressFrom = new InternetAddress(username)
    
    def send(email: Email, recipients: String*) = {
      val addressTo: Array[Address] = recipients.toArray map (x => new InternetAddress(x))
      val session = newSession()
      val msg = newMessage(session) { msg =>
        msg setFrom addressFrom      
        msg.setRecipients(Message.RecipientType.TO, addressTo)      
        msg setSubject email.subject
        msg.setContent(email.body, "text/plain")
        email.headers foreach { case (name, value) => msg.addHeader(name, value) }
      }
      val t = session.getTransport("smtps")
      try {
        t.connect(host, username, password)
        t.sendMessage(msg, msg.getAllRecipients())
      }
      finally t.close()
    }
  }

  case class Email(subject: String, body: String, headers: Seq[(String, String)]) {
    def this(subject: String, body: String) = this(subject, body, Nil)
    def this(body: String) = this("no subject", body, Nil)
  }
}

object EmailTest extends Email {
  def main(args: Array[String]): Unit = {
    val Array(host, username, password, subject, body @ _*) = args
    val server = EmailServer(host, username, password)
    val email = Email(subject, body mkString " ", Nil)
    server.send(email, username)
  }
}
