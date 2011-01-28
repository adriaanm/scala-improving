package improving
package attach

import javax.management._
import openmbean.{ CompositeData, TabularData, TabularType, CompositeType }
import scala.collection.JavaConversions._

trait PrettyPrint {
  self: VM =>
  
  trait PrettyBean {
    self: RichMBean =>
    
    def attributeString() = {
      def getAttr(attr: String) = server.getAttribute(objectName, attr)

      val keys    = attributes map (x => "%s: %s".format(x.getName(), pp.tpe(x.getType())))
      val values  = attributes map (x => pp(getAttr(x.getName())))

      pp.map(keys zip values toMap)
    }
    
    def operationsString(): String = {
      if (operations.isEmpty)
        return ""

      val paramLists  = operations map (op => "def " + op.getName + pp.params(op))
      val returnTypes = operations map (op => pp.tpe(op.getReturnType))
      val len         = paramLists map (_.length) max
      val fmt         = "%" + (len + 1) + "s: %s\n"
      
      (paramLists, returnTypes).zipped map ((x, y) => fmt.format(x, y)) mkString
    }
  }
  
  /** Some ham fisted pretty printing.
   */
  object pp {
    def descriptor(d: Descriptor): String = {
      map(d.getFieldNames() map (x => x -> d.getFieldValue(x)) toMap)
    }
    def params(op: MBeanOperationInfo) = {
      val ps = op.getSignature() map (x => x.getName + ": " + pp.tpe(x.getType))
      
      ps.mkString("(", ", ", ")")
    }
    def sig(op: MBeanOperationInfo) = {
      "%s: %s".format(params(op), pp.tpe(op.getReturnType))
    }
    
    def indent(body: => String) = {
      val lines = (body split "\\n").toList
      lines map ("  " + _) mkString "\n"
    }
    def pair(kv: (Any, Any)): String                = kv._1 + " -> " + pp(kv._2)
    def pair(kv: (Any, Any), keyWidth: Int): String = ("%" + keyWidth + "s -> %s").format(kv._1, pp(kv._2))
    
    def tpe(tp: String): String = {
      if (tp startsWith "[") "Array[%s]".format(tpe(tp drop 1))
      else if (tp startsWith "javax.management.") tp split '.' last
      else if (tp startsWith "java.lang.") tp.stripPrefix("java.lang.")
      else if ((tp startsWith "L") && (tp endsWith ";")) tpe(tp drop 1 dropRight 1)
      else tp.capitalize  // primitives
    }
    
    def map(map: Map[Any, Any]): String = {
      val width = map.keys map (_.toString.length) max
      val pairs = map.toList map (x => pair(x, width + 1) + "\n")
      
      indent(pairs.mkString)
    }

    def composite(data: CompositeData): String = {
      val tp     = data.getCompositeType()
      val keys   = tp.keySet().iterator().toArray[String]
      val types  = keys map (x => pp(tp getType x))
      val values = keys map (x => pp(data get x))
      val len    = keys ++ values map (_.length) sum
      
      if (len < 100) keys zip values map pair mkString ("(", ", ", ")")
      else "((\n" + indent(map(keys zip values toMap)) + "\n))"
    }
    def tabular(data: TabularData): String = {
      val tp     = data.getTabularType()    
      val keys   = tp.getIndexNames().toList
      val values = data.values().toList

      map(keys zip values toMap)
    }

    def apply(body: => Any): String = {
      val value = try body catch { case x => return tpe(x.getClass.getName) + "(\"" + x.getMessage + "\")" }

      value match {
        case x: CompositeData         => composite(x)
        case x: TabularData           => tabular(x)
        case x: java.util.Properties  => map(x.toMap)
        case x: java.util.Map[_, _]   => map(x.toMap)
        case x                        => runtime.ScalaRunTime.stringOf(x).trim
      }
    }
  }
}

