scala> def <[T](xs: T*) = new { def > : List[T] = xs.toList }
$less: [T](xs: T*)java.lang.Object{def >: List[T]}

scala> def *[T](xs: T*) = new { def * : Set[T] = xs.toSet }  
$times: [T](xs: T*)java.lang.Object{def *: Set[T]}

scala> def %[T, U](xs: (T, U)*) = new { def % : Map[T, U] = xs.toMap }
$percent: [T,U](xs: (T, U)*)java.lang.Object{def %: Map[T,U]}

scala> %( 1 -> 2, 5 -> 6, 10 -> 11 )%
res0: Map[Int,Int] = Map((1,2), (5,6), (10,11))

scala> <( 5, 10, 15, 20, 15, 10, 5 )>                                 
res1: List[Int] = List(5, 10, 15, 20, 15, 10, 5)

scala> *(5, 10, 15, 10, 5)*
res2: Set[Int] = Set(5, 10, 15)