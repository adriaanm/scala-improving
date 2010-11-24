package improving
package attach

import javax.management._

trait Beans extends PrettyPrint {
  self: VM =>
  
  class RichMBean(val objectName: ObjectName) extends PrettyBean {
    def this(instance: ObjectInstance) = this(instance.getObjectName())
    def this(name: String) = this(new ObjectName(name))

    val info        = server.getMBeanInfo(objectName)
    val className   = info.getClassName()
    def description = info.getDescription()
    def attributes: List[MBeanAttributeInfo]  = info.getAttributes().toList
    def operations: List[MBeanOperationInfo]  = info.getOperations().toList
    
    def asArray(xs: Any*): Array[AnyRef] = xs map (_.asInstanceOf[AnyRef]) toArray

    def op(opName: String)(params: Any*): Option[Any] = {
      for (op <- operations find (_.getName == opName)) yield {
        val result = server.invoke(objectName, opName, asArray(params: _*), op.getSignature() map (_.getType()))
        if (result == null) ()
        else result
      }
    }
    
    def listen() = {
      try server.addNotificationListener(objectName, PrintingListener, (x: Notification) => true, new java.util.Date())
      catch { case _ => () }
    }

    override def toString = {
      val str =
        if (operations.isEmpty) attributeString()
        else attributeString() + "\n\n    /*** Operations ***/\n\n" + operationsString()

      val fmt =
      """|%s {
         |%s
         |}""".stripMargin
      
      fmt.format(className, str)
    }
  }
}
