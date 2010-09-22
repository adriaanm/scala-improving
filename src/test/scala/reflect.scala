import improving.reflect._

object ReflectionTests {
  // scala> (List(1,2,3): Any).toManifest
  // res0: scala.reflect.Manifest[_] = scala.collection.immutable.$colon$colon
  // 
  // scala> (List(1,2,3): Any).toClass   
  // res1: java.lang.Class[_] = class scala.collection.immutable.$colon$colon
  // 
  // scala> List(1,2,3).toManifest
  // res2: scala.reflect.Manifest[List[Int]] = scala.collection.immutable.$colon$colon
  // 
  // scala> List(1,2,3).toClass   
  // res3: Class[List[Int]] = class scala.collection.immutable.$colon$colon
  // 
  // scala> 1.toManifest
  // res4: scala.reflect.Manifest[Int] = Int
  // 
  // scala> 1.toClass
  // res5: Class[Int] = int
  // 
  // scala> Int.box(1).toManifest  
  // res8: scala.reflect.Manifest[java.lang.Integer] = java.lang.Integer
  // 
  // scala> Int.box(1).toClass   
  // res9: Class[java.lang.Integer] = class java.lang.Integer
}
