package improving
package attach

import com.sun.tools.attach._
import javax.management._
import remote.{ JMXConnectorFactory, JMXServiceURL }
import scala.collection.JavaConversions._
import scala.tools.nsc.io.Path

class Connector(vm: VirtualMachine) {
  val JMXRemotePortProperty    = "com.sun.management.jmxremote.port"
  val JMXConnecterAddrProperty = "com.sun.management.jmxremote.localConnectorAddress"
  
  def props: Map[String, String]    = vm.getAgentProperties().toMap
  def sysProps: Map[String, String] = vm.getSystemProperties().toMap

  def javaHome       = Path(sysProps("java.home"))
  def classPath      = sysProps("java.class.path")
  def defaultAgent   = javaHome / "lib" / "management-agent.jar"
  
  def connect(port: Int) = {
    val address = props.getOrElse(JMXConnecterAddrProperty, {
      vm.loadAgent(defaultAgent.path, JMXRemotePortProperty + "=" + port)
      props(JMXConnecterAddrProperty)
    })
        
    JMXConnectorFactory connect new JMXServiceURL(address)
  }
}
