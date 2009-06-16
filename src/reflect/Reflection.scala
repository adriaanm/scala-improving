// /* Reflection
//  * @author Paul Phillips
//  */
// 
// // package scala.tools.nsc.interpreter
// package org.improving.reflect
// 
// import scala.tools.nsc.util.NameTransformer.{ decode, encode }   // e.g. $plus$plus => ++
// import scala.util.matching.Regex
// import java.lang.{ reflect => r }
// 
// object Reflection {
//   implicit def rmethodToMethod(x: r.Method) = new Method(x)
//   implicit def rttot(x: Class[_]): SClass[_] = new SClass(x)
//   implicit def rttot(rt: r.Type): Type = rt match {
//     case x: r.ParameterizedType   => new ParameterizedType(x)
//     case x: r.GenericArrayType    => new GenericArrayType(x)
//     case x: r.WildcardType        => new WildcardType(x)
//     case x: r.TypeVariable[_]     => new TypeVariable(x)
//     case x: Class[_]              => new SClass(x)
//   }
//   implicit def rtltotl(rts: List[r.Type]): List[Type] = rts map rttot
//   
//   final val sigRegex    = new Regex("""^([^(]+)\((.*?)\)$""", "method", "arglist")
//   final val primitives  = List("boolean", "byte", "short", "int", "long", "float", "double", "char")
// 
//   // Imbuing the java reflection hierarchy with a little more polymorphism
//   sealed abstract class Reflected {
//     def getName(): String
//     def owner: Option[Class[_]] = None
//     def pkg: String = owner.map(_.getPackage.getName + ".") getOrElse ""
//     
//     // string generation goes through here so we can output unqualified
//     // names for types in the same package as the type being inspected
//     final def toScalaString: String = decode(getName)  
//     final def typeToString(x: r.Type) = x.pkgRelativeString(pkg)
//     // final def typesToString(xs: List[r.Type]) = xs.map(typeToString) mkString ", "
//     final def typesToString(xs: List[r.Type]) = {
//       val ps = for ((t, i) <- xs.map(typeToString).zipWithIndex) yield "p%d: %s".format(i + 1, t)
//       ps mkString ", "
//     }
//     
//     final def pkgRelativeString(thePkg: String) = {
//       val str = toScalaString
//       val s = str.replaceAll("""(?!\.)""" + thePkg, "")
//       
//       if (s != str) s
//       else if ((s startsWith "scala.") && s.lastIndexOf('.') == 5) s.substring("scala.".length)
//       else if (s startsWith "java.lang.") s.substring("java.lang.".length)
//       else s
//     }
//   }  
//   
//   trait Modifiable extends Reflected { 
//     def getModifiers(): Int
//     
//     def isStatic        = r.Modifier.isStatic(getModifiers)
//     def isPrivate       = r.Modifier.isPrivate(getModifiers)
//     def isFinal         = r.Modifier.isFinal(getModifiers)
//     def isProtected     = r.Modifier.isProtected(getModifiers)
//     def isSynchronized  = r.Modifier.isSynchronized(getModifiers)
//     def isAbstract      = r.Modifier.isAbstract(getModifiers)
//     def isVolatile      = r.Modifier.isVolatile(getModifiers)
// 
//     def getModifierString =
//       List("private", "protected", "static", "volatile", "abstract", "final", "synchronized")
//       . zip(List(isPrivate, isProtected, isStatic, isVolatile, isAbstract, isFinal, isSynchronized))
//       . filter(_._2 == true)
//       . map(_._1 + " ")
//       . mkString
//   }
//   
//   // Parameterizable (generic arguments): Class, Constructor, Method
//   // Parameterized (actual arguments): ParameterizedType
//   trait Parameterizable extends Reflected {
//     def paramTypes: List[r.Type]
//     def paramString =
//       if (paramTypes.isEmpty) ""
//       else "[" + typesToString(paramTypes) + "]"
//       
//     // From e.g. Function2[B,A,B]  to  (B, A) => B
//     // XXX by name params
//     def functionString = {
//       if (paramTypes.isEmpty) "()"    // this should be impossible
//       else {
//         val args = paramTypes.take(paramTypes.length - 1)
//         "(" + typesToString(args) + ") => " + typeToString(paramTypes.last)
//       }
//     }
//   }
//   
//   /*
//    * Base Class:  Member
//    * Traits:      Modifiable, Parameterizable
//    * Subclasses:  Method, Constructor, Field
//    */
//     
//   abstract class Member(ref: r.Member) extends Reflected with Modifiable  {
//     val  descriptor: String
//     def     retType: Option[r.Type]
//     def formalTypes: List[r.Type]
//     def    excTypes: List[r.Type]
//     def paramString: String
//     
//     def getModifiers() = ref.getModifiers
//     def getIdentifier = decode(ref.getName)
//     override def owner = Some(ref.getDeclaringClass)
//     def argsString: String = "(" + typesToString(formalTypes) + ")"
//     def retString: String = retType.map(x => ": " + typeToString(x)) getOrElse ""
//     def isSynthetic = ref.isSynthetic
//     def getName = /* getModifierString + */ descriptor + " " + getIdentifier + paramString + argsString + retString
//   }
//   
//   class Method(val ref: r.Method) extends Member(ref) with Parameterizable {
//     val descriptor  = "def"
//     def paramTypes  = ref.getTypeParameters.toList
//     def retType     = Some(ref.getGenericReturnType)
//     def formalTypes = ref.getGenericParameterTypes.toList
//     def excTypes    = ref.getGenericExceptionTypes.toList
//     
//     def isStructurallyEqual(m: Method) = {
//       val other = m.ref
//       ref.getName == other.getName &&
//       ref.getReturnType == other.getReturnType &&
//       ref.getParameterTypes.toList == other.getParameterTypes.toList
//     }
//   }
//   
//   class Constructor[T](val ref: r.Constructor[T]) extends Member(ref) with Parameterizable {
//     val descriptor  = "def"
//     def paramTypes  = owner.get.getTypeParameters.toList
//     def retType     = None
//     def formalTypes = ref.getGenericParameterTypes.toList
//     def excTypes    = ref.getGenericExceptionTypes.toList
//     override def getIdentifier = "this"
//   }
//   
//   class Field(val ref: r.Field) extends Member(ref) {
//     val descriptor  = if (isFinal) "val" else "var"
//     def retType     = Some(ref.getGenericType)
//     def formalTypes = Nil
//     def excTypes    = Nil
//     def paramString = ""
//     
//     override def argsString: String = ""
//   }
//   
//   /*
//    * Base Class:  Type
//    * Traits:      Bounded, Parameterizable
//    * Subclasses:  WildcardType, TypeVariable, GenericArrayType, ParameterizedType, SClass
//    */
//   
//   sealed abstract class Type(ref: r.Type) extends Reflected {
//     def getRawName = getName
//     def isJavaLangObject = false
//     def isScalaFunction = false
//   }
//   
//   trait Bounded extends Type {
//     def lower: Array[r.Type]
//     def upper: Array[r.Type]
//     def hasDefaultBounds = upper.length == 1 && upper(0).isJavaLangObject
//     def hasNoLowerBounds = lower == null || lower.length == 0
//     
//     def boundsToString(ts: Array[r.Type], isUpper: Boolean): String = {
//       if (ts == null || ts.length == 0) return ""
//       if (isUpper && ts.length == 1 && ts(0).isJavaLangObject) return ""
//       
//       val boundsStr = if (isUpper) " <: " else " >: "
//       val bounds: List[r.Type] = ts.toList
//       
//       if (bounds.isEmpty) "" else boundsStr + typesToString(bounds)
//     }
//   }
//   
//   
//   class WildcardType(ref: r.WildcardType) extends Type(ref) with Bounded {
//     override def getRawName = "_"
//     def lower = ref.getLowerBounds
//     def upper = ref.getUpperBounds
//     def getName = (hasNoLowerBounds, hasDefaultBounds) match {
//       case (true, true)     => "_"
//       case (true, false)    => typeToString(upper(0))
//       case (false, true)    => boundsToString(lower, false)
//       case (false, false)   => boundsToString(upper, true) + ", " + boundsToString(lower, false)
//     }
//   }
//   
//   class TypeVariable(ref: r.TypeVariable[_]) extends Type(ref) with Bounded {
//     def lower: Array[r.Type] = Nil.toArray
//     def upper = ref.getBounds
//     def getName = ref.getName + boundsToString(upper, true)
//   }
//   
//   class GenericArrayType(ref: r.GenericArrayType) extends Type(ref) {
//     def getName = "Array[" + typeToString(ref.getGenericComponentType) + "]"
//   }
//     
//   class ParameterizedType(ref: r.ParameterizedType) extends Type(ref) with Parameterizable {
//     def paramTypes: List[r.Type] = ref.getActualTypeArguments.toList
//     override def isScalaFunction = ref.getRawType.isScalaFunction
//     override def getRawName = ref.getRawType.getRawName
//     override def getName = 
//       if (isScalaFunction) functionString
//       else getRawName + paramString
//   }
//   
//   class SClass[T](val ref: Class[T]) extends Type(ref) with Modifiable with Parameterizable {
//     def getModifiers  = ref.getModifiers
//     def paramTypes    = ref.getTypeParameters.toList    
//     def methods       = ref.getDeclaredMethods.toList map { m => new Method(m) }
//     def constructors  = ref.getDeclaredConstructors.toList map { c => new Constructor(c) }
//     def fields        = ref.getDeclaredFields.toList map { f => new Field(f) }
//     def classes       = ref.getDeclaredClasses.toList map { c => new SClass(c) }
//     def declarations: List[Member] = (methods ::: fields) filter (!_.isPrivate)  
//     
//     override def pkg  = ref.getPackage.getName
//     override def isJavaLangObject = ref == classOf[AnyRef]
//     override def isScalaFunction = ref.getCanonicalName startsWith "scala.Function"    
//     override def owner = if (ref.getDeclaringClass == null) Some(ref) else Some(ref.getDeclaringClass)
//     
//     // method signatures for all methods matching name (i.e. overloaded set)
//     def signatures(name: String)  = methods.filter(_.getIdentifier == name).map(_.toScalaString)      
//     def constructorSignatures     = constructors.map(_.toScalaString)   
//     
//     override def getName = getRawName + paramString
//     override def getRawName =
//       if (ref.getSimpleName endsWith "[]") mkArrayString(ref.getSimpleName)
//       else mkScalaString(ref.getName)
//     
//     private def mkArrayString(s: String): String =
//       if (s endsWith "[]") "Array[" + mkArrayString(s.substring(0, s.length - 2)) + "]" else s
//     
//     private def mkScalaString(s: String): String = {   
//       if (s == "void") "Unit"
//       else if (primitives contains s) s.capitalize
//       else if (isJavaLangObject) "AnyRef"
//       else s
//     }
//   }
//   
//   // classloading
//   def getClassByName(s: String, cl: ClassLoader): Option[Class[_]] = { 
//     // println("getClassByName: " + s)
//     try   { Some(Class.forName(s, false, cl)) }
//     catch { case _: ClassNotFoundException | _: NoClassDefFoundError => None }
//   }
// }
