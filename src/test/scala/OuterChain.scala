// 
// scala> import improving._
// import improving._
// 
// scala> class A { class B { class C } } ; val a = new A ; val b = new a.B ; val c = new b.C
// defined class A
// a: A = A@2834ac60
// b: a.B = A$B@1a20e73f
// c: b.C = A$B$C@662de67f
// 
// scala> c.outerChain
// res0: List[AnyRef] = List(A$B$C@662de67f, A$B@1a20e73f, A@2834ac60)
// 
