package improving
package attach

import com.sun.tools.attach._
import javax.management._
import scala.collection.JavaConversions._

object VM {
  def apply(id: String): VM = new VM(VirtualMachine attach id)
  def apply(id: Int): VM    = apply(id.toString)
}

class VM(val vm: VirtualMachine) extends Beans {
  def id       = vm.id()
  def provider = vm.provider()
  def pid      = toInt(id)
  def port     = 5000
  
  val connector = new Connector(vm)
  def server: MBeanServerConnection = connector.connect(port).getMBeanServerConnection()
  
  def mbeans = server.queryMBeans(null, null).toSet[ObjectInstance] map (x => new RichMBean(x))
  def ops    = mbeans flatMap (_.operations)
  def attrs  = mbeans flatMap (_.attributes)
  
  def listenAll() = mbeans foreach (_.listen())
    
  def hotspotBean = mbeans find (_.className == "sun.management.HotSpotDiagnostic") get
  def dumpHeap(file: String) = {
    hotspotBean.op("dumpHeap")(file, true)
  }

  override def toString = "VM(%s: %s)".format(id, vmMap(id))
}
