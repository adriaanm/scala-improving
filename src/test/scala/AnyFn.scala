package improving.function

import org.scalacheck._
import Prop._

// It's a start.
object NumericFnSpec extends Properties("NumericFn") {
  val fn = NumericFn[Int](
    x => x + 1 toInt ,
    x => x toInt ,
    _ => 0
  )
  
  property("Ints")    = forAll((x: Int) => fn(x) == x + 1)
  property("Floats")  = forAll((x: Float) => fn(x).toInt == x.toInt)
  property("AnyRefs") = forAll((x: List[Int]) => fn(x) == 0)
}


