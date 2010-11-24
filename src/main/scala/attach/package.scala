package improving

import com.sun.tools.attach._
import spi.AttachProvider
import scala.collection.JavaConversions._
import javax.management._

package object attach {  
  def providers: List[AttachProvider]             = AttachProvider.providers().toList
  def descriptors: List[VirtualMachineDescriptor] = VirtualMachine.list.toList
  def vmMap: Map[String, String]                  = descriptors map (x => x.id -> x.displayName) toMap
  def vmIds: List[String]                         = descriptors map (_.id())

  def toInt(s: String) = try { s.toInt } catch { case _: NumberFormatException => -1 }
  
  implicit def functionToFilter[T](f: Notification => Boolean): NotificationFilter =
    new NotificationFilter {
      def isNotificationEnabled(notification: Notification) = f(notification)
    }
}