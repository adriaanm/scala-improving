object Benchmark {
  case class Run[T](result: T, time: Long)
  
  def time[T](body: => T): Run[T] = {
    val t1 = System.currentTimeMillis
    val res = body
    val t2 = System.currentTimeMillis
    
    Run(res, t2 - t1)
  }
  
  // def compare[T,U](reps: Int, body1: () => T, body2: () => U): (Run[T], Run[U]) = {
  def compare[T,U](reps: Int, body1: => T, body2: => U): (Run[T], Run[U]) = {
    val res1 @ Run(result1, time1) = time(body1)
    val res2 @ Run(result2, time2) = time(body2)
    
    def stats() = {
      val speedup = Math.abs(time2 - time1) * 100 / (time1 max time2)
      val winner = if (time2 > time1) "First" else "Second"
      
      if (speedup == 0) "Virtual tie."
      else "%s body %d%% faster".format(winner, speedup)
    }
    
    println(stats())
    (res1, res2)
  }
}