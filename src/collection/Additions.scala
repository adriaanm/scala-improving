package org.improving.collection

object Additions {
  implicit def improvingDotOrgEnrichStream[T](x: Stream[T]): RichStream[T] = new RichStream(x)
  implicit def improvingDotOrgEnrichList[T](x: List[T]): RichList[T] = new RichList(x)
  
  class RichStream[T](xs: Stream[T]) {
    def tails(): Stream[Stream[T]] =
      if (xs.isEmpty) Stream.cons(Stream.empty, Stream.empty)
      else Stream.cons(xs, xs.tail.tails)
  }
  
  class RichList[T](xs: List[T]) {
    private def assemble(rest: => List[List[T]]) =
      if (xs.isEmpty) List(Nil)
      else xs :: rest
    
    def tails(): List[List[T]] = assemble(xs.tail.tails)
    def inits(): List[List[T]] = assemble(xs.init.inits)
  }
}