package improving
package reflect
// 
// object o {
//   import java.{ lang => jl }
//   import scala.reflect.{ Manifest => M, AnyValManifest => AVM }
//   import scala.{ runtime => rt }
//   
//   class RichAnyVal[T <: AnyVal] {
//     type Boxed <: jl.Number
//     
//     def box(x: T): Boxed
//     def unbox(x: Boxed): T
//   }
//   
//   object AnyValLists {
//     val anyValNames: List[String] = List(
//       "Byte", "Short", "Int", "Long", "Float", "Double", "Char", "Boolean", "Unit"
//     )
//     val anyValCompanions: List[rt.AnyValCompanion] = List(
//       rt.Byte, rt.Short, rt.Int, rt.Long, rt.Float, rt.Double, rt.Char, rt.Boolean, rt.Unit
//     )
//     val anyValTypes: List[Class[_]] = List(
//       classOf[Byte], classOf[Short], classOf[Int], classOf[Long], classOf[Float], classOf[Double], classOf[Char], classOf[Boolean], classOf[Unit]
//     )
//     val anyValBoxes: List[Class[_]] = List(
//       
//     )
//   }
//   
//   // class Boxing[T <: scala.AnyVal](val valClass: Class[T], val refClass: Class[_]) {
//   class Boxing[T, U](val primMan: AVM[T], refMan: Manifest[U]) {
//     def boxed = manifest.erasure
//     def unboxed = 
//   }
//     
//     val valClass: Class[T], val refClass: Class[_]) {  
//     def valName: String = valClass.getName.capitalize
//     def refName: String = refClass.getName
//     override def toString = valName + "/" + refName
//   }
//   // 
//   // class Boxing[T <: AnyVal, U <: AnyRef](val valClass: Class[T], val refClass: Class[U]) {
//   //   def name: String = valClass.getName.capitalize
//   //   def boxName: String = refClass.getName
//   // }
//   object Boxing {
//     val anyValCompanionMap: Map[] = Map()
//     
//     def anyValManifest[T <: AnyVal : Manifest] : AVM[T] = manifest[T] match {
//       case x: AVM[_]    => x.asInstanceOf[AVM[T]]
//       case x            => null
//     }
//     def anyValBoxedManifest[T <: AnyVal : Manifest] = Manifest.classType(manifest[T].erasure)
//     def anyValCompanion[T <: AnyVal : Manifest] : AnyValCompanion = 
// 
//     implicit object Byte extends Boxing(classOf[scala.Byte], classOf[jl.Byte])
//     implicit object Short extends Boxing(classOf[scala.Short], classOf[jl.Short])
//     implicit object Int extends Boxing(classOf[scala.Int], classOf[jl.Integer])
//     implicit object Long extends Boxing(classOf[scala.Long], classOf[jl.Long])
//     implicit object Float extends Boxing(classOf[scala.Float], classOf[jl.Float])
//     implicit object Double extends Boxing(classOf[scala.Double], classOf[jl.Double])
//     implicit object Boolean extends Boxing(classOf[scala.Boolean], classOf[jl.Boolean])
//     implicit object Char extends Boxing(classOf[scala.Char], classOf[jl.Character])
//     implicit object Unit extends Boxing(classOf[scala.Unit], classOf[jl.Void])
//     implicit object AnyVal extends Boxing[scala.AnyVal](classOf[scala.AnyVal], classOf[scala.AnyRef])
//     
//     val all: List[Boxing[_]] = List(Byte, Short, Int, Long, Float, Double, Boolean, Char, Unit)
//     val allMap: Map[Class[_], Boxing[_]] = Map[Class[_], Boxing[_]]() ++ {
//       all map (x => x.valClass -> x)
//     }
//     
//     implicit def apply[T <: AnyVal : Manifest] : Boxing[T] = allMap(manifest[T].erasure).asInstanceOf[Boxing[T]]
//     // 
//     // 
//     // 
//     // implicit def anyvalBoxing[T <: AnyVal]: Boxing[T] = {
//     //   reflect.Manifest.singleType()
//     //   all find (_.valClass == manifest[T].erasure)
//     // }
//     // 
//     // private val allPlus: List[Boxing[_]] = AnyVal :: all
//     // private val classMap: Map[Class[_], Boxing[_]] = {      
//     //   val xs1: List[(Class[_], Boxing[_])] = all.map[(Class[_], Boxing[_]), List[(Class[_], Boxing[_])]](x => x.valClass -> x)
//     //   val xs2: List[(Class[_], Boxing[_])] = all.map[(Class[_], Boxing[_]), List[(Class[_], Boxing[_])]](x => x.refClass -> x)
//     // 
//     //   (xs1 ++ xs2) toMap
//     // }
//     // 
//     // def apply[T <: scala.AnyVal](clazz: Class[T]): Boxing[T] = classMap(clazz).asInstanceOf[Boxing[T]]
//     
//     // def apply[T <: AnyVal] : Boxing[T] = classMap 
//   }
// }
// 
// trait AnyValExtensions[T <: AnyVal : Manifest] {
//   def primitiveClass[T <: AnyVal] : Class[T] = 
//   def boxedClass[T <: AnyVal] : C
//   
//   def box(value: T): Number = 
//   
//   def box[T <: AnyVal] : Number = {
//     
//   }
//   
//     val clazz = x.getSort() match {
//       case VOID     => "Unit"
//       case BOOLEAN  => "Boolean"
//       case CHAR     => "Char"
//       case BYTE     => "Byte"
//       case SHORT    => "Short"
//       case INT      => "Int"
//       case FLOAT    => "Float"
//       case LONG     => "Long"
//       case DOUBLE   => "Double"
//       case ARRAY    => arrayTypeToString(x)
//       case _        => x.getInternalName.toExternal
//     }
//   
// }