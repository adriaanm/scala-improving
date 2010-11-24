package improving
package attach

import javax.management._
import openmbean.{ CompositeData, TabularData, TabularType, CompositeType }
import scala.collection.JavaConversions._


case object PrintingListener extends NotificationListener {
  def handleNotification(notification: Notification, handback: AnyRef): Unit = {
    println("handleNotification(%s, %s)".format(notification, handback))
  }
}
